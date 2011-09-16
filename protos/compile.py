#!/usr/bin/env python
import os
import sys
import argparse
import subprocess
import re

# Might be lower, but not tested. Definitely needs to be > 2.3.0
MIN_PROTOC_VERSION = '2.4.1'
SERVICES = {
  'console_logger-2.0': 'ConsoleLoggerProtos',
  'cookie_manager-1.0': 'CookieMngProtos',
  'core_1.3': 'CoreUtilsProtos',
  'desktop_utils': 'DesktopUtilsProtos',
  'desktop_window_manager': 'DesktopWmProtos',
  'ecmascript-1.0': 'EcmascriptProtos',
  'ecmascript_debugger-6.1': 'Esdbg6Protos',
  'ecmascript_debugger-5.0': 'EsdbgProtos',
  'exec-2.1': 'ExecProtos',
  'http_logger-2.0': 'HttpLoggerProtos',
  'prefs-1.0': 'PrefsProtos',
  'scope-1.0': 'ScopeProtos',
  'selftest': 'SelftestProtos',
  'system_input': 'SystemInputProtos',
  'ums': 'UmsProtos',
  'window_manager-2.1': 'WmProtos',
}

RESULT = {
  'INVALID_PROTOC': 1,
  'INVALID_SERVICE': 2,
  'PROTOC_ERROR': 3
}

def main():
  parser = argparse.ArgumentParser(description='Compile protobuf files with Opera-specific extensions')
  parser.add_argument('--protoc', metavar='path', type=str, default='protoc',
                     help='Path to the protoc binary. Must be version > 2.4.1')
  parser.add_argument('--java_out', metavar='path', type=str, default='.',
                     help='Directory to output the compiled .proto files')
  parser.add_argument('proto_file', type=str, default='protoc', nargs='+',
                     help='.proto files to compile')

  args = parser.parse_args()

  if not check_protoc_version(args.protoc):
    return RESULT['INVALID_PROTOC']

  for fname in args.proto_file:
    # Get the Java class name
    (name, _) = os.path.splitext(fname)
    if not name in SERVICES:
      sys.stderr.write("Error: Service %s is not known. Known services: %s\n" % (name, SERVICES.keys()))
      return RESULT['INVALID_SERVICE']
    else:
      service_class = SERVICES[name]

    with open(fname) as f:
      content = f.read()

    # Pre-process
    content = remove_scope_syntax(content)
    content = remove_service_block(content)
    content = add_options(content, service_class)

    # TODO: Create a proper temp file?
    with open('temp.proto', 'w') as f:
      f.write(content)

    print "Compiling %s (as temp.proto)..." % fname
    if not compile_proto(args.protoc, args.java_out, 'temp.proto'):
      return RESULT['PROTOC_ERROR']

  print "Successfully compiled %d files" % len(args.proto_file)
  return 0

def compile_proto(protoc, out, fname):
  retcode = subprocess.call([protoc, '--java_out', out, fname])
  if retcode != 0:
    sys.stderr.write("Error: Protoc error when compiling %s\n" % fname)
    return False
  return True


def remove_scope_syntax(content):
  return re.sub(r'^syntax = "?scope"?;', '', content)

def remove_service_block(content):
  lines = content.split('\n')
  # tiny state machine :)
  found_service = False

  start = end = 0
  brackets = 0

  for i in range(0, len(lines)):
    if not found_service:
      # TODO: better matching
      if lines[i].startswith('service'):
        start = i
        found_service = True
    else:
      # Can have opening and closing brackets on the same line
      # FIXME: Will pick up brackets in commments
      if '{' in lines[i]: brackets += 1
      if '}' in lines[i]: brackets -= 1
      # The first line after the service line will have 1 bracket. This then
      # will not drop to 0 until the last bracket in the service block
      if brackets == 0:
        end = i+1
        break;

  del lines[start:end]

  return '\n'.join(lines)

def add_options(content, service_class):
  options = """package scope;

option java_package = "com.opera.core.systems.scope.protos";
option java_outer_classname = "%s";
option optimize_for = SPEED;
""" % service_class

  return options + content

def check_protoc_version(path):
  # No subprocess.check_output() in python 2.6 :(
  version = subprocess.Popen([path, '--version'], stdout=subprocess.PIPE).communicate()[0].strip()
  version = version[len('libprotoc '):]

  if not version_greater_or_equal_to(version.split('.'), MIN_PROTOC_VERSION.split('.')):
    sys.stderr.write("Error: Minimum protoc version is %s, %s is %s\n" % (MIN_PROTOC_VERSION, path, version))
    return False

  return True

def version_greater_or_equal_to(version, min_version):
  """Compares two version strings and returns True if version > min_version

  >>> version_greater_or_equal_to('1.0', '0')
  True
  >>> version_greater_or_equal_to('1.0', '0.9.9.9')
  True
  >>> version_greater_or_equal_to('1.0', '2.0')
  False
  >>> version_greater_or_equal_to('0.9.9.9', '1.0')
  False
  >>> version_greater_or_equal_to('1.0', '1.0')
  True
  >>> version_greater_or_equal_to('9.8.7.6.5.4.32.1', '9.8.7.6.5.4.3.1')
  True
  """
  for i in range(0, min(len(min_version), len(version))):
    if version[i] < min_version[i]:
      return False
    elif version[i] > min_version[i]:
      return True
    # if equal we need to check the next least-significant number

  # All checked numbers were equal, but version could be smaller if it is
  # shorter, e.g. 2.4 < 2.4.1
  return len(version) <= len(min_version)

if __name__ == '__main__':
  sys.exit(main())
