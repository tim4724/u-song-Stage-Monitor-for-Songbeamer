<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.TutorialView" -->
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=0.15, maximum-scale=0.15, user-scalable=0"/>
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/tutorial.css">
    <script src="/assets/js/settings.js"></script>
    <title>${messages.getString("tutorial")}</title>
</head>

<body onload="connectToWebSocket()">
<main>
    <h1 id="title">${messages.getString("welcome")}</h1>

    <h2 class="tutorialHeadline"><span class="tutorialNumber">1 </span>${messages.getString("tutorial1")}</h2>

    <img src="/assets/img/tutorial.png">

    <div id="tutorialRemote">
        <h2 class="tutorialHeadline"><span class="tutorialNumber">2a </span>${messages.getString("tutorial2a")}</h2>

        <div class="setting">
            <a href="http://${hostname}" target="_blank">http://${hostname}</a>

            <#if ipAddress != "0.0.0.0" && ipAddress != "127.0.0.1">
                <div> ${messages.getString("or")} </div>
                <a href="http://${ipAddress}" target="_blank">http://${ipAddress}</a>
            </#if>
        </div>
    </div>
    <div id="tutorialLocal">
        <h2 class="tutorialHeadline"><span class="tutorialNumber">2b </span>${messages.getString("tutorial2b")}</h2>
        <div class="setting">
            <label>
                <#if (getScreensCount() > 1)>
                    <select id="showOnDisplay" onchange="new function() {onInputChanged('showOnDisplay')};">
                        <option value="-1" ${(getFullscreenDisplay() == -1)?then("selected", "")}>
                            ${messages.getString("showNotFullscreen")}
                        </option>
                        <#list 1..screensCount as i>
                            <option value="${i-1}" ${(getFullscreenDisplay() == i-1)?then("selected", "")}>
                                ${messages.getString("display")} ${i}
                            </option>
                        </#list>
                    </select>
                <#else>
                    <span class="disabled">
                        ${messages.getString("showFullscreenNotAvailable")}
                    </span>
                </#if>
            </label>
        </div>
    </div>
</main>
</body>
</html>
