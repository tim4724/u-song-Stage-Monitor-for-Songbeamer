<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.SongView" -->
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=0.15, maximum-scale=0.15, user-scalable=0"/>
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/song.css">
    <link rel="stylesheet" href="/assets2/css/song2.css">
    <#if admin>
        <link rel="stylesheet" href="/assets/css/admin.css">
    </#if>
    <script src="/assets/js/zenscroll.js"></script>
    <script src="/assets/js/main.js"></script>
    <title>${song.getTitle()}</title>
</head>

<body onload="backend = main(); backend.connectToWebSocket()" class="notranslate">
<header>
    <h1 id="title" data-songId="#{song.hashCode()}" <#if song.type.name() == "ERROR"> class="negative" </#if>>
        ${song.title}
        <#if chords && song.keyChord??>
            <span class="chord">${messages.getString("key")} ${song.keyChord.toHtml()}</span>
        </#if>
    </h1>
</header>

<#list song.sections as section>
    <section class="${section.type}">
        <#if section.name??>
            <div class="sectionName">${section.name}</div>
        </#if>
        <#list section.pages as page>
            <div class="page">
                <#if (page.linesCount > 0)>
                    <div class="pageContent">
                        <#if chords>
                            ${page.toHtmlWithCords()}
                        <#else>
                            ${page.toHtml()}
                        </#if>
                    </div>
                </#if>
            </div>
        </#list>
    </section>
</#list>

<#if (song.sections?size == 0) >
    <#include "clock.ftl" >
<#else>
    <#if !admin && isShowClockInSong()>
        <div id="clockInSong">
            <#include "clock.ftl">
        </div>
    </#if>
</#if>

<div id="bottomSpacer"></div>

<#if admin>
    <footer id="controlsWrapper">
        <span id="activeClients">-</span>
        <button type="button" id="upButton" onclick="backend.pageUp()">&#9650;</button>
        <button type="button" id="downButton" onclick="backend.pageDown()">&#9660;</button>
        <#if (song.langCount > 1)>
            <#list 1..song.langCount as i>
                <button type="button" <#if i == song.lang> class="active" disabled</#if>
                        onclick="backend.setLang(${i});">${messages.getString("language")} ${i}</button>
            </#list>
        </#if>
        <button type="button" id="clock">&#128340;</button>
    </footer>
</#if>

<div id="errorBox">
    &#9888; ${messages.getString("connectionLost")}
</div>

<script>
    window.onerror = function (e) {
        document.getElementById("errorBox").style.display = 'block';
        console.error(e);
    };
</script>

</body>
</html>
