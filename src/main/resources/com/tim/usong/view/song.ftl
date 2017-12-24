<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.SongView" -->
<html>
<head>
    <meta name="viewport" content="width=device-width,initial-scale=0.15"/>
    <link rel="stylesheet" href="/assets/css/song.css">
    <link rel="stylesheet" href="/assets2/css/song2.css">
<#if admin>
    <link rel="stylesheet" href="/assets/css/admin.css">
</#if>
    <script src="/assets/js/update.js"></script>
    <script src="/assets/js/zenscroll.js"></script>
    <script>
        var titleElement = undefined;
        var pages = undefined;
        var currentPage = undefined;
        var lastPageNumber = -1;

        function init() {
            window.titleElement = document.getElementById('title');
            window.pages = document.getElementsByClassName('page');
            update();
        }
    </script>
</head>

<body onload="init()">
<header>
    <h1 id="title">
    <#if song.title?? && song.title?has_content>${song.title}</#if>
    </h1>
</header>

<#list song.sections as section>
<section class="${section.type}">
    <#if section.name??>
        <div class="sectionName">${section.name}</div>
    </#if>
    <#list section.pages as page>
        <div class="page">
        ${page.content}
        </div>
    </#list>
</section>
</#list>

<div id="bottomSpacer"></div>

<#if admin>
    <#include "admin-controls.ftl">
</#if>

</body>
</html>
