#!/usr/bin/env python

# Stuart Knightley <stuartk@opera.com>

import urllib2
from BeautifulSoup import BeautifulSoup, Tag, NavigableString
import re

drivers = {
#  'Chrome': 'http://www.jarvana.com/jarvana/view/org/seleniumhq/selenium/selenium-chrome-driver/2.0a2/selenium-chrome-driver-2.0a2-javadoc.jar!/org/openqa/selenium/chrome/ChromeDriver.html',
  'EventFiring': 'http://webdriver.googlecode.com/svn/javadoc/org/openqa/selenium/support/events/EventFiringWebDriver.html',
  'Firefox': 'http://webdriver.googlecode.com/svn/javadoc/org/openqa/selenium/firefox/FirefoxDriver.html',
  'HtmlUnit': 'http://webdriver.googlecode.com/svn/javadoc/org/openqa/selenium/htmlunit/HtmlUnitDriver.html',
  'IE': 'http://webdriver.googlecode.com/svn/javadoc/org/openqa/selenium/ie/InternetExplorerDriver.html',
  'Opera': 'http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/OperaDriver.html',
  'Remote': 'http://webdriver.googlecode.com/svn/javadoc/org/openqa/selenium/remote/RemoteWebDriver.html',
  'Safari': 'http://webdriver.googlecode.com/svn/javadoc/org/openqa/selenium/safari/SafariDriver.html'
}

methods = {}
inherited = {}

def get_methods(driver, docs):
  # Load the document and parse
  try:
    soup = BeautifulSoup(urllib2.urlopen(docs).read())
  except (urllib2.HTTPError):
    print "Could not get docs for %s from %s" % (driver, docs)
    return

  # Find all method names that are inherited, and store the interface they
  # come from. Later we will pair the methods up.
  # Although this way is computationally slower ( O(n^2) ), it it far easier
  # for me, and computer time is cheap!
  for specified in soup.findAll('b', text='Specified by:'):
    method = specified.findParent('dl').findParent('dl').findPreviousSibling('h3').text
    #interface = specified.findParent('dt').findNextSibling('dd').findAll('code')[1].text
    interface = specified.findParent('dl').findAll('code')[-1].text
    if not method in inherited: inherited[method] = interface

  # Get the table of methods
  table = soup.find('a', {'name': 'method_summary'}).findNextSibling('table')

  # Loop through the rows, clean up the method and add to our table
  for row in table.findAll('tr'):
    tds = row.findAll('td')
    # Exclude the headers, which only have one td
    if len(tds) == 2:
      method = tds[1].code
      if method == None: continue
      method = ' '.join(method.text.replace('&nbsp;', ' ').replace('java.lang.', '').split())

      # Remove argument names as these are different between drivers
      method = re.sub(r" ?[a-zA-Z]+,", ",", method)
      method = re.sub(r" ?[a-zA-Z]+\)", ")", method)

      # Have we seen this method before?
      if method in methods:
        # Yes, add this driver as supported
        methods[method][driver] = True
      else:
        # No, add this driver as supported...
        methods[method] = {driver: True}
        # ...and check if it's been inherited from somewhere
        for interface_method, interface in inherited.items():
          if method.startswith(interface_method+"("):
            methods[method]['_interface'] = interface

def generate(methods):
  # Template to use for generation
  gen = BeautifulSoup("""<!doctype html>
  <html>
  <head>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
   <title>API support in Web Driver implementations</title>
   <style type="text/css">
  body {
    font: 0.9em/145% georgia,times,serif;
  }

  table {
    font-family: verdana, sans-serif;
    font-size: 0.8em;
    text-align: center;
    vertical-align: middle;

    border: 1px solid #666;
    border-collapse: collapse;
  }

  td, th {
    padding: 0.5em;
    border: 1px solid #666;
    border-collapse: collapse;
  }

  th {
    background: #EEE;
  }

  td:first-child {
    text-align: right;
  }


  td.yes {
    background: #DFD;
  }
   </style>
  </head>
  <body><table id="support"></table></body></html>""")
  table = gen.find("table")

  # Utility function to allow us to easily append arbitrary HTMl to Soup elements
  def appendEl(el, html):
    new = BeautifulSoup(html)
    el.insert(len(el), new)
    return new.first()

  # Create the heading for the table
  tr = appendEl(table, "<tr></tr>")
  appendEl(tr, "<th>Method</th>")
  # Add all the drivers to the heading
  for driver, docs in drivers.items():
    appendEl(tr, """<th><a href="%s">%s</a></th>""" % (docs, driver))
  appendEl(tr, "<th>Interface</th>")

  prevInterface = ""

  # Loop through all the methods, checking off support in various drivers
  for method in methods:
    tr = appendEl(table, '<tr/>')
    appendEl(tr, "<td>%s</td>" % method['method'])

    support = method['support']
    for driver in drivers.keys():
      if driver in support and support[driver] == True:
        appendEl(tr, """<td class="yes">yes</td>""")
      else:
        appendEl(tr, "<td></td>")

    if '_interface' in method['support']:
      appendEl(tr, "<td>%s</td>" % method['support']['_interface'])
    else:
      appendEl(tr, "<td></td>")

  return gen

############# Main #############

for driver, docs in drivers.items():
  get_methods(driver, docs)

methods_list = [{'method': k, 'support': v} for k, v in methods.items()]

def comp(a, b):
  # sort on amount of support
  x = len(b['support']) - len(a['support'])
  if not x == 0: return x
  # Sort in interface name
  if '_interface' in a['support'] and '_interface' in b['support']:
    x = cmp(a['support']['_interface'], b['support']['_interface'])
  if not x == 0: return x

  # If proprietary sort on driver name
  if len(a['support']) == 1 and len(b['support']) == 1:
    x = cmp(a['support'].keys()[0], b['support'].keys()[0])
  if not x == 0: return x

  return cmp(a['method'], b['method'])

methods_sorted = sorted(methods_list, cmp = comp)

gen = generate(methods_sorted)

print gen
