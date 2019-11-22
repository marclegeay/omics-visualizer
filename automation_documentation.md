# Omics Visualizer Automation Documentation

Omics Visualizer version 1.2

Last update: 2019-11-13

## List of commands

- [List filter operators](#filter-list-operators)
- [List palettes](#palette-list)
- [Version](#version)

### Manage tables

- [Connect a table with a network](#connect)
- [Disconnect](#disconnect)
- [Apply a filter](#filter)
- [Delete a filter](#filter-remove)
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

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Connect show

`ov connect show`

Show the connect window of the current table

[List of commands](#list-of-commands) - [List of Access GUI dialogs commands](#access-gui-dialogs)

## Disconnect

`ov disconnect`

Disconnect the current table and the current network if they are already connected

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

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

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

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

[List of commands](#list-of-commands)

## Filter remove

`ov filter remove`

Removes the filter of the active table

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Filter show

`ov filter show`

Show the filter window of the current table

[List of commands](#list-of-commands) - [List of Access GUI dialogs commands](#access-gui-dialogs)

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
   
- `showTitle` (optional) *boolean* Default: `false`

  Should the title be added to the legend?
   
- `title` (optional) *String* Default: the network name

  The title of the legend.  
  If you don't want any title, you give an empty title.

### Example

`ov legend draw orientation="VERTICAL" position="EAST_TOP" title=""`

This will generate a legend containing all the visualizations (inner and/or outer, if any) with no title. The legend will be drawned vertically at the right of the network, aligned with the top of it.

[List of commands](#list-of-commands) - [List of Legends commands](#legends)

## Legend hide

`ov legend hide`

Hide a legend

[List of commands](#list-of-commands) - [List of Legends commands](#legends)

## Load

`ov load`

Load an Omics Visualizer table.  
Returns the name of the newly imported OV table.

### Arguments

- `dataTypeList` (optional) *String*

   List of column data types ordered by column index (e.g. "string,int,long,double,boolean,intlist" or just "s,i,l,d,b,il").  
   If not given, the types will be automatically detected.
   
- `decimalSeparator` (optional) *Character* Default: `'.'`

   Character that separates the integer-part (characteristic) and the fractional-part (mantissa) of a decimal number.
   
- `delimiters` (optional) *String* Default: `","` if CSV, `TAB` otherwise

   Select the delimiters to use to separate columns in the table.  
   Must be one of:
   - `","`
   - `" "`
   - `"\t"`
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

`ov load file="/path/to/myfile.csv" newTableName="myData"`

This loads the file "myfile.csv" into Omics Visualizer and create the corresponding table with the name "myData".

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Palette list

`ov palette list`

List available palettes with their provider

[List of commands](#list-of-commands)

## Retrieve

`ov retrieve`

Retrieve a STRING network and connects it to the active OV table.

### Arguments

- `cutoff` (optional) *Double* Default: 0.4

  Confidence (score) cutoff. Must be between 0.0 and 1.0.
  
- `filteredOnly` (optional) *boolean* Default: `true`

  Only filtered rows should be used to the query?
  
- `queryColumn` **(required)** *String*

  Column name of the Omics Visualizer Table.
  
- `selectedOnly` (optional) *boolean* Default: `false`

  Only selected rows should be used to the query ?
  
- `species` (optional) *String*
  
  Name of the species to query.
  
- `taxonID` (optional) *int*

  Identifier of the species to query.

***Nota Bene:*** `species` and `taxonID` are optional, but at least one of them should be filled.

### Example

`ov retrieve queryColumn="UniProt" taxonID="9606"`

This retrieves a STRING network for the active OV table. The query is made on UniProt values from the OV table for Human (taxon `9606`).

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Retrieve show

`ov retrieve show`

Show the retrieve window of the current table

[List of commands](#list-of-commands) - [List of Access GUI dialogs commands](#access-gui-dialogs)

## Table delete

`ov table delete`

Delete the active Omics Visualizer table

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Table list

`ov table list`

Get the list of Omics Visualizer tables

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Table set current

`ov table set current`

Set the current Omics Visualizer table

### Argument

- `tableName` **(required)** *String*

   Name of the OV table to set as active

### Example

`ov table set current tableName="myData"`

This sets the OV table named "myData" as the active one.

[List of commands](#list-of-commands) - [List of Manage tables commands](#manage-tables)

## Version

`ov version`

Returns the current version of the app

[List of commands](#list-of-commands)

## Viz apply inner continuous

`ov viz apply inner continuous`

Apply an inner visualization (pie charts) with a continuous mapping

### Arguments

- `attributes` **(required)** *String*

  Name of the column that contains the data you want to visualize.
  
- `chartSettings` (optional) *String*

  Comma separated list of [enhancedGraphics settings](http://www.rbvi.ucsf.edu/cytoscape/utilities3/enhancedcg.shtml).  
  Here is how the string should be formatted: "setting1:value1,setting2:value2" e.g. "arcstart:0,arcdirection:counterclockwise"
  
- `colorMax` (optional) *String*

  Color used in the gradient as the highest value.
  
- `colorMid` (optional) *String*

  Color used in the gradient as the middle value.
  
- `colorMin` (optional) *String*

  Color used in the gradient as the lowest value.
  
- `colorMissing` (optional) *String*

  Color used for missing values.
  
- `filteredOnly` (optional) *boolean* Default: `true`

  Use all the data (`false`) or only the filtered one (`true`)?
  
- `labels` (optional) *String*

  Column name of the table that should be used to label the data. By default no label is displayed.
  
- `paletteName` (optional) *String*

  Name of the palette to use as default colors. (See [ov palette list](#palette-list))
  
- `paletteProviderName` (optional) *String*

  Name of the palette provider of the palette. (See [ov palette list](#palette-list))
  
- `rangeMax` (optional) *double*

  Maximum value. Above this value, the same color will be applied.
  
- `rangeMid` (optional) *double*

  Middle value. The colors will be a gradient from min to mid, and from mid to max.
  
- `rangeMin` (optional) *double*

  Minimum value. Below this value, the same color will be applied.

### Examples

`ov viz apply inner continuous attributes="log ratio" rangeMin=-5 rangeMax=5`

This will create a pie chart visualization from the "log ratio" column, using the default palette colors with a range from -5 to 5.

`ov viz apply inner continuous attributes="log ratio" rangeMin=-5 rangeMax=5 paletteName="Purple-Orange" paletteProviderName="ColorBrewer"`

This will create a pie chart visualization from the "log ratio" column, using the specified palette colors with a range from -5 to 5.

`ov viz apply inner continuous attributes="log ratio" rangeMin=-5 rangeMax=5 colorMin="blue" colorMid="white" colorMax="red"`

This will create a pie chart visualization from the "log ratio" column, using the specified colors with a range from -5 to 5.

[List of commands](#list-of-commands) - [List of Visualizations commands](#visualizations)

## Viz apply inner discrete

`ov viz apply inner discrete`

Apply an inner visualization (pie charts) with a discrete mapping

### Arguments

- `attributes` **(required)** *String*

  Name of the column that contains the data you want to visualize.
  
- `chartSettings` (optional) *String*

  Comma separated list of [enhancedGraphics settings](http://www.rbvi.ucsf.edu/cytoscape/utilities3/enhancedcg.shtml).  
  Here is how the string should be formatted: "setting1:value1,setting2:value2" e.g. "arcstart:0,arcdirection:counterclockwise"
  
- `colorMapping` (optional) *String*

  Comma separated values of mappings value:color. Special characters in values must be escaped.

- `filteredOnly` (optional) *boolean* Default: `true`

  Use all the data (`false`) or only the filtered one (`true`)?
  
- `labels` (optional) *String*

  Column name of the table that should be used to label the data. By default no label is displayed.
  
- `paletteName` (optional) *String*

  Name of the palette to use as default colors. (See [ov palette list](#palette-list))
  
- `paletteProviderName` (optional) *String*

  Name of the palette provider of the palette. (See [ov palette list](#palette-list))

### Example

`ov viz apply inner discrete attributes="cluster" colorMapping="A:blue,B:orange,C:red"`

  This will create a pie chart visualization with the "cluster" column. The value "A" will be blue, "B" orange and "C" red. If there are more values in the "cluster" column, a default color from the default palette will be assigned.

[List of commands](#list-of-commands) - [List of Visualizations commands](#visualizations)

## Viz apply outer continuous

`ov viz apply outer continuous`

Apply an outer visualization (donuts charts) with a continuous mapping

### Arguments

- `attributes` **(required)** *String*

  Comma separated value list of attributes from the table you want to visualize the data. Special characters in the attributes must be escaped.
  
- `chartSettings` (optional) *String*

  Comma separated list of [enhancedGraphics settings](http://www.rbvi.ucsf.edu/cytoscape/utilities3/enhancedcg.shtml).  
  Here is how the string should be formatted: "setting1:value1,setting2:value2" e.g. "arcstart:0,arcdirection:counterclockwise"
  
- `colorMax` (optional) *String*

  Color used in the gradient as the highest value.
  
- `colorMid` (optional) *String*

  Color used in the gradient as the middle value.
  
- `colorMin` (optional) *String*

  Color used in the gradient as the lowest value.
  
- `colorMissing` (optional) *String*

  Color used for missing values.
  
- `filteredOnly` (optional) *boolean* Default: `true`

  Use all the data (`false`) or only the filtered one (`true`)?
  
- `labels` (optional) *String*

  Column name of the table that should be used to label the data. By default no label is displayed.
  
- `paletteName` (optional) *String*

  Name of the palette to use as default colors. (See [ov palette list](#palette-list))
  
- `paletteProviderName` (optional) *String*

  Name of the palette provider of the palette. (See [ov palette list](#palette-list))
  
- `rangeMax` (optional) *double*

  Maximum value. Above this value, the same color will be applied.
  
- `rangeMid` (optional) *double*

  Middle value. The colors will be a gradient from min to mid, and from mid to max.
  
- `rangeMin` (optional) *double*

  Minimum value. Below this value, the same color will be applied.
  
- `transpose` (optional) *boolean* Default: `false`
  
  Should the ring represents a column (`false`) or a row (`true`)?

### Example

`ov viz apply outer continuous attributes="log1,log2,log3" rangeMin=-2 rangeMax=2`

This will create a donut chart visualization with three rings: "log1", "log2", and "log3". The colors are automatically assigned according to the default palette with a range from -2 to 2.

[List of commands](#list-of-commands) - [List of Visualizations commands](#visualizations)

## Viz apply outer discrete

`ov viz apply outer discrete`

Apply an outer visualization (donuts charts) with a discrete mapping

### Arguments

- `attributes` **(required)** *String*

  Comma separated value list of attributes from the table you want to visualize the data. Special characters in the attributes must be escaped.
  
- `chartSettings` (optional) *String*

  Comma separated list of [enhancedGraphics settings](http://www.rbvi.ucsf.edu/cytoscape/utilities3/enhancedcg.shtml).  
  Here is how the string should be formatted: "setting1:value1,setting2:value2" e.g. "arcstart:0,arcdirection:counterclockwise"
  
- `colorMapping` (optional) *String*

  Comma separated values of mappings value:color. Special characters in values must be escaped.

- `filteredOnly` (optional) *boolean* Default: `true`

  Use all the data (`false`) or only the filtered one (`true`)?
  
- `labels` (optional) *String*

  Column name of the table that should be used to label the data. By default no label is displayed.
  
- `paletteName` (optional) *String*

  Name of the palette to use as default colors. (See [ov palette list](#palette-list))
  
- `paletteProviderName` (optional) *String*

  Name of the palette provider of the palette. (See [ov palette list](#palette-list))
  
- `transpose` (optional) *boolean* Default: `false`
  
  Should the ring represents a column (`false`) or a row (`true`)?

### Example

`ov viz apply outer discrete attributes="cluster1,cluster2"`

This will create a donut chart visualization with two rings: the "cluster1" and "cluster2" values. The colors for each value will be automatically generated using the default palette.

[List of commands](#list-of-commands) - [List of Visualizations commands](#visualizations)

## Viz remove inner

`ov viz remove inner`

Remove the inner Visualization (pie charts) of the current network

[List of commands](#list-of-commands) - [List of Visualizations commands](#visualizations)

## Viz remove outer

`ov viz remove outer`

Remove the outer Visualization (donuts charts) of the current network

[List of commands](#list-of-commands) - [List of Visualizations commands](#visualizations)

## Viz show inner

`ov viz show inner`

Show the inner visualization (pie charts) window of the current table

[List of commands](#list-of-commands) - [List of Access GUI dialogs commands](#access-gui-dialogs)

## Viz show outer

`ov viz show outer`

Show the outer visualization (donuts charts) window of the current table

[List of commands](#list-of-commands) - [List of Access GUI dialogs commands](#access-gui-dialogs)
