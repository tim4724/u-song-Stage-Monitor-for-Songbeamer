<!DOCTYPE html>
<#-- @ftlvariable name="" type="com.tim.usong.view.TutorialView" -->
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=0.15, maximum-scale=0.15, user-scalable=0"/>
    <link rel="shortcut icon" href="/assets/favicon.ico">
    <link rel="stylesheet" href="/assets/css/tutorial.css">
    <title>${messages.getString("tutorial")}</title>
</head>

<body>
<main>
    <h1 id="title">${messages.getString("welcome")}</h1>

    <h2 class="tutorialHeadline">${messages.getString("tutorial1")}</h2>

    <img src="/assets/img/tutorial.png">

    <h2 class="tutorialHeadline">${messages.getString("tutorial2")}</h2>

    <div id="urls">
        <a href="http://${hostname}" target="_blank">http://${hostname}</a>

        <#if ipAddress != "0.0.0.0" && ipAddress != "127.0.0.1">
            <span> ${messages.getString("or")} </span>
            <a href="http://${ipAddress}" target="_blank">http://${ipAddress}</a>
        </#if>
    </div>
</main>
</body>
</html>
