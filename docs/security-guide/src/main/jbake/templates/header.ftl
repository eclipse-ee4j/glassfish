<!DOCTYPE html>
<#-- a header template fragment included in the page template -->
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape></#if></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/style.css" rel="stylesheet">
    <script src="https://use.fontawesome.com/96c4d89611.js"></script>
  </head>
  <body>
<table id="doc-title" cellspacing="0" cellpadding="0">
  <tr>
  <td align="left" valign="top">
  <b>${content.title}</b><br />
  </td>
  </tr>
</table>
<hr />

<table width="90%" id="top-nav" cellspacing="0" cellpadding="0">
	<colgroup>
		<col width="12%"/>
		<col width="12%"/>
		<col width="*"/>
	</colgroup>
	<tr>
	    <#if content.prev??>
		<td align="left">
		<a href="${content.prev}">
			<span class="vector-font"><i class="fa fa-arrow-circle-left" aria-hidden="true"></i></span>
			<span style="position:relative;top:-2px;">Previous</span>
		</a>
		</td>
	    </#if>

	    <#if content.next??>
		<td align="left">
		<a href="${content.next}">
			<span class=" vector-font"><i class="fa fa-arrow-circle-right vector-font" aria-hidden="true"></i></span>
			<span style="position:relative;top:-2px;">Next</span>
		</a>
		</td>
	    </#if>

		<td align="right">
		<a href="toc.html">
			<span class=" vector-font"><i class="fa fa-list vector-font" aria-hidden="true"></i></span>
			<span style="position:relative;top:-2px;">Contents</span>
		</a>
		</td>
	</tr>
</table>

