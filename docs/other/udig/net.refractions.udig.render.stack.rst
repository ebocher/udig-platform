Net.refractions.udig.render.stack
#################################

+-------------+-------------+-------------+-------------+-------------+-------------+-------------+-------------+-------------+
| uDig :      |
| net.refract |
| ions.udig.r |
| ender.stack |
| This page   |
| last        |
| changed on  |
| Jul 14,     |
| 2012 by     |
| jgarnett.   |
| Visualizati |
| on          |
| Stack       |
| merges all  |
| the         |
| rasters(Ima |
| ge          |
| Buffers)    |
| created by  |
| the         |
| rendering   |
| pipelines   |
| with JAI    |
| and updates |
| the viewing |
| area.       |
|             |
| Functional  |
| Requirement |
| s:          |
| ''''''''''' |
| ''''''''''' |
| ''          |
|             |
| -  Merges   |
|    context  |
|    rasters  |
| -  Merges   |
|    selectio |
| n           |
|    rasters  |
| -  Merges   |
|    decorato |
| rs          |
| -  Outputs  |
|    a final  |
|    raster   |
|    that     |
|    will be  |
|    displaye |
| d           |
|    on       |
|    screen   |
| -  Manages  |
|    cached   |
|    rasters  |
| -  Notifies |
|    `net.ref |
| ractions.ud |
| ig.project. |
| selection < |
| net.refract |
| ions.udig.p |
| roject.sele |
| ction.html> |
| `__         |
|    which    |
|    layers   |
|    are      |
|    modified |
|    and the  |
|    bbox (in |
|    screen   |
|    coords)  |
|    of the   |
|    selectio |
| n           |
|             |
| Non-functio |
| nal Require |
| ments:      |
| ''''''''''' |
| ''''''''''' |
| ''''''      |
|             |
| -  Allows   |
|    `net.ref |
| ractions.ud |
| ig.project. |
| selection < |
| net.refract |
| ions.udig.p |
| roject.sele |
| ction.html> |
| `__         |
|    to       |
|    perform  |
|    a        |
|    "quick"  |
|    selectio |
| n           |
|    on the   |
|    cached   |
|    rasters  |
|             |
| Design note |
| s:          |
| ''''''''''' |
| ''          |
|             |
| -  Takes    |
|    the      |
|    rasters  |
|    from the |
|    renderer |
| s,          |
|    their    |
|    selectio |
| n           |
|    equivale |
| nts         |
|    and      |
|    merges   |
|    them     |
|    into one |
|    final    |
|    raster.  |
| -  Quick    |
|    notifica |
| tion        |
|    of       |
|    selectio |
| n           |
|    activity |
|    can be   |
|    done by  |
|    applying |
|    the      |
|    selectio |
| n           |
|    style to |
|    the      |
|    portion  |
|    of the   |
|    features |
|    within   |
|    the      |
|    selected |
|    rubber-b |
| and.        |
             
+-------------+-------------+-------------+-------------+-------------+-------------+-------------+-------------+-------------+

+------------+----------------------------------------------------------+
| |image1|   | Document generated by Confluence on Aug 11, 2014 12:31   |
+------------+----------------------------------------------------------+

.. |image0| image:: images/border/spacer.gif
.. |image1| image:: images/border/spacer.gif
