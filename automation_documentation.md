# Omics Visualizer Automation Documentation

Omics Visualizer version 1.2

Last update: 2019-10-17

## List of commands

- [Version](#version)

### Manage tables

- [Connect a table with a network](#connect)
- [Disconnect](#disconnect)
- [Filter](#filter)
- [Load](#load)
- [Retrieve a STRING network](#retrieve)
- [Delete a table](#table-delete)
- [List tables](#table-list)
- [Change the current table](#table-set-current)

### Visualizations

- [Pie chart with continuous mapping](#viz-apply-inner-continuous)
- [Pie chart with discrete mapping](#viz-apply-inner-discrete)
- [Donut chart with continuous mapping](#viz-apply-outer-continuous)
- [Donut chart with discrete mapping](#viz-apply-outer-discrete)
- [Remove pie chart](#viz-remove-inner)
- [Remove donut chart](#viz-remove-outer)

### Legends

- [Create/reload a legend](#legend-draw)
- [Delete a legend](#legend-hide)

### Access GUI dialogs

- [Connect](#connect-show)
- [Filter](#filter-show)
- [Retrieve](#retrieve-show)
- [Inner visualization](#viz-show-inner)
- [Outer visualization](#viz-show-outer)

## Connect

`ov connect`

Connect the current table with the current network

### Arguments

- `mappingColNet` **(required)** *String*

   Key column from the Network node table used to map the network with the table

- `mappingColTable` **(required)** *String*

   Key column from the Omics Visualizer table used to map the table with the network

### Example

`ov connect mappingColNet="gene name" mappingColTable="name"`

This will link the active OV table with the current network. The OV table must have a column named `name` and the node table from the network must have a column named `gene name`. Those two columns are used to link the two tables.

## Connect show

`ov connect show`

Show the connect window of the current table

## Disconnect

`ov disconnect`

Disconnect the current table and the current network if they are already connected

## Filter

`ov filter`

Filters the row of an Omics Visualizer table

### Arguments

- `filter` **(required)** *String*

   The filter is defined by the following non-contextual grammar:  
   filter = and | or | criteria  
   and = {filter_list}  
   or = [filter_list]  
   filter_list = filter,filter_list | filter  
   criteria = (colName,operator,value)  
   
   where 'colName' is the name of the column (commas in colName should be escaped by being preceeded a backslash)  
   'operator' is the name of the operation applied, the list of operators is available with the command [`ov filter list operators`](#filter-list-operators)  
   'value' if necessary, is the value to compare with. Careful with the regex and escaped characters.

- `tableName` (optional) *String*

   OV table name.  
   By default the active table is used, if you want to use another one you can specify its name here.

### Example

`ov filter filter="{(p-value,LOWER_EQUALS,0.05),[(ontology,CONTAINS,GO),(ontology,NULL,)]}"`

This will select the rows where the p-value is lower or equals to 0.05 and the ontology is either from the Gene Ontology (contains "GO") or unknown (the value is null).

## Filter list operators

`ov filter list operators`

List the available operators

The list of current available operators is:
- `EQUALS` (*String*, *boolean*, *numbers*) the table value should match exactly the reference value
- `NOT_EQUALS` (*String*, *boolean*, *numbers*) the table value should not match the reference value
- `CONTAINS` (*String*) a part of the table value should match exactly the reference value
- `NOT_CONTAIN` (*String*) a part of the table value should match exactly the reference value
- `MATCHES` (*String*) the table value should match exactly the regular expression from the reference value
- `LOWER` (*numbers*) the table value should be strictly lower than the reference value
- `LOWER_EQUALS` (*numbers*) the table value should be lower than or equals to the reference value
- `GREATER` (*numbers*) the table value should be strictly greater than the reference value
- `GREATER_EQUALS` (*numbers*) the table value should be greater than or equals to the reference value
- `NULL` (*String*, *boolean*, *numbers*) the table value should be null
- `NOT_NULL` (*String*, *boolean*, *numbers*) the table value should not be null

## Filter remove

`ov filter remove`

Removes the filter of the active table

## Filter show

`ov filter show`

Show the filter window of the current table

## Legend draw

`ov legend draw`

Draw a legend

### Arguments

- `fitView` (optional) *boolean* Default: `true`

   Should the view of the network be fitted to the content after the legend is added.
   
- `fontName` (optional) *String* Default: `"SansSerif"`

   The font family.

- `fontSize` (optional) *int* Default: `22`

   The font size.
   
- `includeInner` (optional) *boolean* Default: `true` if there is an inner visualization, `false` otherwise

   Should the inner visualization included in the legend?
- `includeOuter` (optional) *boolean* Default: `true` if there is an outer visualization, `false` otherwise

   Should the outer visualization included in the legend?
   
- `orientation` (optional) *String* Default: `"HORIZONTAL"`

   The orientation of the legend.  
   Must be one of:  
   - `"HORIZONTAL"`
   - `"VERTICAL"`

- `position` (optional) *String* Default: `"NORHT_LEFT"`

   The position of the legend.  
   Must be one of:  
   - `"NORTH"` the legend will be on top of the network, centered
   - `"NORTH_LEFT"` the legend will be on top of the network, aligned left
   - `"NORTH_RIGHT"` the legend will be on top of the network, aligned right
   - `"SOUTH"` the legend will be at the bottom of the network, centered
   - `"SOUTH_LEFT"` the legend will be at the bottom of the network, aligned left
   - `"SOUTH_RIGHT"` the legend will be at the bottom of the network, aligned right
   - `"EAST"` the legend will be on the right of the network, aligned with the middle
   - `"EAST_TOP"` the legend will be on the right of the network, aligned top
   - `"EAST_BOTTOM"` the legend will be on the right of the network, aligned bottom
   - `"WEST"` the legend will be on the left of the network, aligned with the middle
   - `"WEST_TOP"` the legend will be on the left of the network, aligned top
   - `"WEST_BOTTOM"` the legend will be on the left of the network, aligned bottom
   
- `title` (optional) *String* Default: the network name

  The title of the legend.  
  If you don't want any title, you give an empty title.

## Example

`ov legend draw orientation="VERTICAL" position="EAST_TOP" title=""`

This will generate a legend containing all the visualizations (inner and/or outer, if any) with no title. The legend will be drawned vertically at the right of the network, aligned with the top of it.

## Legend hide

`ov legend hide`

Hide a legend

## Load

`ov load`

Load an Omics Visualizer table

### Arguments

- `dataTypeList` (optional) *String*

   List of column data types ordered by column index (e.g. "string,int,long,double,boolean,intlist" or just "s,i,l,d,b,il")
   
- `decimalSeparator` (optional) *Character* Default: `'.'`

   Character that separates the integer-part (characteristic) and the fractional-part (mantissa) of a decimal number.
   
- `delimiters` (optional) *String* Default: `","` if CSV, `TAB` otherwise

   Select the delimiters to use to separate columns in the table.  
   Must be one of:
   - `","`
   - `" "`
   - `"TAB"`
   - `";"`
   
- `delimitersForDataList` (optional) *Character* Default: `'|'`

   The delimiters between elements of list columns in the table.  
   Must be one of:
   - `'|'`
   - `'\'`
   - `'/'`
   - `','`
   
- `file` **(required)** *String*

  The path to the file that contains the table or network to be imported.
  
- `firstRowAsColumnNames` (optional) *boolean* Default: `true`

  Does the first imported row contains column names?
  
- `newTableName` (optional) *String*

   The title of the new table.  
   If no title is given, then a name will be generated, started by "Omics Visualizer Table" followed by a number.
   
- `startLoadRow` (optional) *int* Default: `1`

   The first row of the input table to load. This allows the skipping of headers that are not part of the import.

### Example

## Palette list

`ov palette list`

List available palettes with their provider

## Retrieve

`ov retrieve`

Retrieve a STRING network and connects it to the current table

## Retrieve show

`ov retrieve show`

Show the retrieve window of the current table

## Table delete

`ov table delete`

Delete the current Omics Visualizer table

## Table list

`ov table list`

Get the list of Omics Visualizer tables

## Table set current

`ov table set current`

Set the current Omics Visualizer table

## Version

`ov version`

Returns the current version of the app

## Viz apply inner continuous

`ov viz apply inner continuous`

Apply an inner visualization (pie charts) with a continuous mapping

## Viz apply inner discrete

`ov viz apply inner discrete`

Apply an inner visualization (pie charts) with a discrete mapping

## Viz apply outer continuous

`ov viz apply outer continuous`

Apply an outer visualization (donuts charts) with a continuous mapping

## Viz apply outer discrete

`ov viz apply outer discrete`

Apply an outer visualization (donuts charts) with a discrete mapping

## Viz remove inner

`ov viz remove inner`

Remove the inner Visualization (pie charts) of the current network

## Viz remove outer

`ov viz remove outer`

Remove the outer Visualization (donuts charts) of the current network

## Viz show inner

`ov viz show inner`

Show the inner visualization (pie charts) window of the current table

## Viz show outer

`ov viz show outer`

Show the outer visualization (donuts charts) window of the current table
