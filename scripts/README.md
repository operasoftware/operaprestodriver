WebDriver comparison table generator
====================================

Simply call

    ./comparison.py > comparison.html

How it works
------------

Briefly, it stores a list of the html javadoc pages for each driver. It then
uses Beautiful Soup to parse the methods the driver supports, and where they
are inherited from. After some sorting it then uses Beautiful Soup again to
generate the table.
