function main() {
    const titleElement = document.getElementById('title');
    const pages = document.getElementsByClassName('page');
    const errorElement = document.getElementById("errorBox");
    const clientsCountElement = document.getElementById("activeClients");
    const songId = parseInt(titleElement.getAttribute('data-songId'));
    let currentPage = undefined; // element
    let lastPageNumber = -1; // number

    function setLang(newLang) {
        let xhr = new XMLHttpRequest();
        xhr.open('POST', 'song/lang/' + newLang, true);
        xhr.send();
    }

    function setPage(newPage) {
        let xhr = new XMLHttpRequest();
        xhr.open('POST', 'song/page/' + newPage, true);
        xhr.send();
    }

    function connectToWebSocket() {
        const ws = new WebSocket("ws://" + location.host + "/song/ws");
        ws.onopen = function () {
            errorElement.style.display = "none";
        };
        ws.onmessage = function (ev) {
            let data = JSON.parse(ev.data);
            if (songId !== parseInt(data.songId)) {
                location.reload(true);
            }
            if (lastPageNumber !== data.page) {
                updatePageNumber(data.page);
            }
            if (clientsCountElement) {
                if (data.clients === 1) {
                    clientsCountElement.classList.add('negative');
                } else {
                    clientsCountElement.classList.remove('negative');
                }
                clientsCountElement.innerText = data.clients;

            }
        };
        ws.onclose = function (ev) {
            setTimeout(connectToWebSocket, 500);
            console.error("ws closed" + ev.reason);
            errorElement.style.display = "block";
        };
    }

    function updatePageNumber(newPageNumber) {
        const oldPage = currentPage;
        currentPage = pages[newPageNumber];

        if (oldPage) {
            oldPage.parentNode.classList.remove('currentSection');
        } else {
            //no page is set yet
            if (newPageNumber === -1) {
                scrollTo(0, 0);//scroll to the top of the page
                return;
            }
        }

        if (currentPage) {
            if (oldPage) {
                oldPage.classList.remove('currentPage');
            }
            lastPageNumber = newPageNumber;
            currentPage.classList.add('currentPage');
            currentPage.parentElement.classList.add('currentSection');

            let offset = 10;
            let scrollTarget;
            if (newPageNumber === 0) {
                scrollTarget = titleElement;
            } else if (currentPage.parentElement.scrollHeight < (innerHeight * 0.8)) {
                scrollTarget = currentPage.parentNode;
            } else {
                scrollTarget = currentPage;
                if (currentPage.parentElement.getElementsByClassName('page')[0] !== currentPage) {
                    offset = innerHeight * 0.2;
                }
            }

            let duration = isVisible(currentPage) ? 2000 : 300;
            scroll(scrollTarget, offset, duration);
        }
    }

    return {
        setLang: setLang,
        pageUp: function () {
            return setPage(lastPageNumber - 1);
        },
        pageDown: function () {
            return setPage(lastPageNumber + 1);
        },
        connectToWebSocket: connectToWebSocket
    }
}

function scroll(e, offset, duration) {
    zenscroll.setup(duration, offset);

    zenscroll.to(e, duration);
    setTimeout(function () {
        if (!zenscroll.moving()) {
            zenscroll.to(e, duration);
        }
    }, 100);//sometimes its buggy and doesnt scroll right away

    //backup plan
    setTimeout(function () {
        if (!zenscroll.moving()) {
            e.scrollIntoView(true);
        }
    }, 200);
}

function isVisible(el) {
    const elemTop = el.getBoundingClientRect().top;
    const elemBottom = el.getBoundingClientRect().bottom;
    return (elemTop >= 0) && (elemBottom <= innerHeight);
}