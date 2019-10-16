# Omics Visualizer Automation Documentation

Omics Visualizer version 1.2

Last update: 2019-10-16

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

## Connect show

`ov connect show`

Show the connect window of the current table

## Disconnect

`ov disconnect`

Disconnect the current table and the current network if they are already connected

## Filter

`ov filter`

Filters the row of an Omics Visualizer table

## Filter list operators

`ov filter list operators`

List the available operators

## Filter remove

`ov filter remove`

Removes the filter of the active table

## Filter show

`ov filter show`

Show the filter window of the current table

## Legend draw

`ov legend draw`

Draw a legend

## Legend hide

`ov legend hide`

Hide a legend

## Load

`ov load`

Load an Omics Visualizer table

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
