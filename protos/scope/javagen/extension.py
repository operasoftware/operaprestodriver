"""
javagen

Generates Java-compatible .proto files from protocol buffer definitions for use
with protoc.
"""

from copy import copy
from hob.cmd import CommandTable, ProgramError, argparse
from hob.extension import ExtensionError
from hob.proto import Package, Request, Event, Proto, Service, Command, Message, Field, Enum, Option, OptionValue
from hob.template import TextGenerator
from hob.utils import _
import hob
import os
import re
import shutil
import sys

def check_version(ui):
    from distutils.version import LooseVersion as Version
    if not hasattr(ui, 'version') or not Version(ui.version) > '0.2':
        raise ExtensionError('javagen extension requires hob 0.2 or higher')

cmds = CommandTable()

@cmds.add([
        ('', 'out', '',
            _('write generated code to specified directory (default: "./out")')),
        ],
        [(('service', 'services'), [],
            _('files to transform into Google Protobuffer compatible .proto files for use with Java')),
        ])
def java(ui, services, out, **kwargs):
    """Generate Protocol Buffer definitions which are Java compatible"""

    # Use java.conf so that we don't pollute hob's namespace
    java_config = readJavaConfig('./java.conf')

    from hob.proto import PackageManager
    f = sys.stdout
    if out:
        if not os.path.exists(out):
            os.makedirs(out)
    elif out:
        f = open(out, 'w')

    if not services:
        services = list(ui.config.currentTarget().services())

    if services:
        if out:
            for extra_file, extra_dir in (('proto/scope_descriptor.proto', 'opera/scope'), ('proto/google/protobuf/descriptor.proto', 'google/protobuf')):
                extra_dir = os.path.normpath(os.path.join(out, extra_dir))
                if not os.path.exists(extra_dir):
                    os.makedirs(extra_dir)
                root = os.path.dirname(os.path.abspath(hob.cmd.__file__))
                fname = os.path.join(extra_dir, os.path.basename(extra_file))
                shutil.copyfile(os.path.join(root, 'templates', extra_file), fname)
                ui.outl('Copied %s to %s' % (extra_file, fname))

    text = ''
    for fname in services:
        try:
            if os.path.isfile(fname):
                manager = PackageManager()
                package = manager.loadFile(fname)
            else:
                package = ui.config.currentTarget().findPackage(fname)
        except IOError, e:
            raise ProgramError('Cannot open proto file %s: %s' % (fname, e.strerror))

        package = applyOptions(package, java_config)

        gen = TextGenerator()
        text = gen.package(package, export=['package', 'message', 'enum'])

        if out:
            if package.services:
                out_name = package.services[0].name
            elif package.name:
                out_name = package.name
            else:
                out_name = fname
            fname = os.path.join(out, out_name + '.proto')
            outfile = open(fname, 'w').write(text)
            ui.outl('Wrote service %s to %s' % (package.services[0].name, fname))
        else:
            f.write(text)

def readJavaConfig(java_config_file):
    """ Reads the Java config entries from the specified file and returns a
    ConfigParser object """
    import ConfigParser
    java_config = ConfigParser.SafeConfigParser()
    if os.path.exists(java_config_file):
        java_config.read([java_config_file])
    return java_config

def applyOptions(package, config):
    if package.services:
        service = package.services[0]

    default_package = packageName(package)

    default_options = {
        'package_name': 'scope',
        'Package': {
            'java_package': 'com.opera.core.systems.scope.protos',
            'java_outer_classname': default_package + 'Protos',
            'optimize_for': 'SPEED'
        }
    }

    options = default_options

    # Read options from conf file and update options
    for element in (Package, Service, Command, Request, Event, Message, Field, Enum, Option):
        if element.__name__ not in options:
            options[element.__name__] = {}

        def updateOptions(optionstext):
            proto_options = {}
            for option in optionstext.split(';'):
                if not option.strip(): # skip empty entries
                    continue
                name, value = option.split(':', 1)
                proto_options[name.strip()] = value.strip()
            options[element.__name__].update(proto_options)

        # protoc related options are stored in _ (underscore) + fieldname
        if config.has_option('options', element.__name__):
            updateOptions(config.get('options', element.__name__))
        if config.has_option(service.name + '.options', element.__name__):
            updateOptions(config.get(service.name + '.options', element.__name__))

    for (item, value) in options.get('Package').iteritems():
        # hob doesn't encapsulate FooBar with quotes, forcing this behaviour.
        # But it should not encapsulate enums, such as SPEED.
        if re.match(r'^[A-Z]+$', value):
            package.options[item] = OptionValue(value)
        else:
            package.options[item] = OptionValue(None, raw='"' + value + '"')

    if options.get('package_name'):
        package.name = options.get('package_name')

    return package

def packageName(package):
    """ Gets package name from given package based on the file name, defaulting
    to 'scope' """
    default_package = 'scope'
    package_name = packageNameFromFileName(package.filename)
    if not package_name:
        package_name = default_package
    return package_name

def packageNameFromFileName(filename):
    return os.path.splitext(os.path.basename(filename))[0].title()
