#!/usr/bin/env python

# Copyright 2011-2012 Opera Software ASA
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import sys
import argparse
import subprocess
import re
import tempfile

# Might be lower, but not tested. Definitely needs to be > 2.3.0
MIN_PROTOC_VERSION = '2.4.1'
SERVICES = {
  'console_logger': ['com.opera.core.systems.scope.protos', 'ConsoleLoggerProtos'],
  'cookie_manager': ['com.opera.core.systems.scope.protos', 'CookieMngProtos'],
  'core': ['com.opera.core.systems.scope.protos', 'CoreProtos'],
  'desktop_utils': ['com.opera.core.systems.scope.protos', 'DesktopUtilsProtos'],
  'desktop_window_manager': ['com.opera.core.systems.scope.protos', 'DesktopWmProtos'],
  'ecmascript': ['com.opera.core.systems.scope.protos', 'EcmascriptProtos'],
  'ecmascript_debugger-6.1': ['com.opera.core.systems.scope.protos', 'Esdbg6Protos'],
  'ecmascript_debugger-5.0': ['com.opera.core.systems.scope.protos', 'EsdbgProtos'],
  'exec': ['com.opera.core.systems.scope.protos', 'ExecProtos'],
  'http_logger': ['com.opera.core.systems.scope.protos', 'HttpLoggerProtos'],
  'prefs': ['com.opera.core.systems.scope.protos', 'PrefsProtos'],
  'scope': ['com.opera.core.systems.scope.protos', 'ScopeProtos'],
  'selftest': ['com.opera.core.systems.scope.protos', 'SelftestProtos'],
  'system_input': ['com.opera.core.systems.scope.protos', 'SystemInputProtos'],
  'ums': ['com.opera.core.systems.scope.protos', 'UmsProtos'],
  'window_manager': ['com.opera.core.systems.scope.protos', 'WmProtos'],

  # NOTE: This is not a scope protocol and the generated Java must be moved
  # to src/com/opera/core/systems/runner/launcher/
  'launcher': ['com.opera.core.systems.runner.launcher', 'OperaLauncherProtos']
}

RESULT = {
  'INVALID_PROTOC': 1,
  'INVALID_SERVICE': 2,
  'PROTOC_ERROR': 3
}

def main():
  parser = argparse.ArgumentParser(description='Compile protobuf files with Opera-specific extensions')
  parser.add_argument('--protoc', metavar='path', type=str, default='protoc',
                      help='Path to the protoc binary (must be version > 2.4.1)')
  parser.add_argument('--java_out', metavar='path', type=str, default='.',
                      help='Directory to output the compiled .proto files')
  parser.add_argument('proto_file', type=str, default='protoc', nargs='+',
                      help='.proto files to compile')
  parser.add_argument('--preprocess_only', default=False, action='store_true',
                      help='Only do the preprocessing step, do not call protoc')
  parser.add_argument('--preprocess_out', metavar='path', type=str, default='./preprocess_out',
                      help='Directory to output the preprocessed .proto files')

  args = parser.parse_args()

  if not args.preprocess_only and not check_protoc_version(args.protoc):
    return RESULT['INVALID_PROTOC']

  # Ant gives us a single argument with the paths separated by ":" on Linux
  # and ";" on Windows. This seperater is stored in os.pathsep. If we get an
  # argument that looks like this, split it.
  if len(args.proto_file) == 1 and os.pathsep in args.proto_file[0]:
    args.proto_file = args.proto_file[0].split(os.pathsep)

  if args.preprocess_only and not os.path.exists(args.preprocess_out):
    os.mkdir(args.preprocess_out)

  for fname in args.proto_file:
    # Get the Java class name
    (name, _) = os.path.splitext(fname)
    name = os.path.basename(name)
    if not name in SERVICES:
      sys.stderr.write("Error: Service %s is not known. Known services: %s\n" % (name, SERVICES.keys()))
      return RESULT['INVALID_SERVICE']
    else:
      service_package = SERVICES[name][0]
      service_class = SERVICES[name][1]

    with open(fname) as f:
      content = f.read()

    # Pre-process
    content = remove_scope_syntax(content)
    content = remove_service_block(content)
    content = add_options(content, service_package, service_class)

    if args.preprocess_only:
      preprocessed_file = os.path.join(args.preprocess_out, '%s.proto' % name)
    else:
      preprocessed_file = tempfile.NamedTemporaryFile().name

    # Write preprocessed content to file and compile the files
    t = tempfile.NamedTemporaryFile()
    try:
      t.write(content)
      t.flush()  # make sure file is written properly

    if not args.preprocess_only:
      print "Compiling %s" % fname
      if not compile_proto(args.protoc, args.java_out, preprocessed_file):
        return RESULT['PROTOC_ERROR']

  print "Successfully compiled %d files" % len(args.proto_file)
  return 0

def compile_proto(protoc, out, file):
  proto_path = os.path.abspath(os.path.join(file, os.path.pardir))
  fname = os.path.basename(file)

  retcode = subprocess.call([protoc, '--proto_path', proto_path, '--java_out', out, file])
  if retcode != 0:
    sys.stderr.write("Error: protoc error when compiling %s\n" % fname)
    return False
  return True

def remove_scope_syntax(content):
  # Quotes are optional
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

def add_options(content, service_package, service_class):
  options = """package scope;

option java_package = "%s";
option java_outer_classname = "%s";
option optimize_for = SPEED;
""" % (service_package, service_class)

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
