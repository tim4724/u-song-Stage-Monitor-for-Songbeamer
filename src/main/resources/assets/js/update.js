function update() {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', '/song/page', true);
    xhr.onload = function () {
        window.errorElement.style.display = "none";
        if (xhr.status === 205) {
            location.reload(true);
            return;
        }
        try {
            if (xhr.status === 200) {
                updatePageNumber(parseInt(xhr.responseText));
            }
            update()
        } catch (e) {
            window.errorElement.style.display = "block";
            console.error(e);
            setTimeout(update(), 500);
        }
    };
    xhr.onerror = function (e) {
        window.errorElement.style.display = "block";
        console.error(e);
        setTimeout(update(), 500);
    };
    xhr.send(null);
}

function updatePageNumber(newPageNumber) {
    const oldPage = window.currentPage;
    window.currentPage = window.pages[newPageNumber];

    if (oldPage) {
        oldPage.parentNode.classList.remove('currentSection');
    } else {
        //no page is set yet
        if (newPageNumber === -1) {
            scrollTo(0, 0);//scroll to the top of the page
            return;
        }
    }

    if (window.currentPage) {
        if (oldPage) {
            oldPage.classList.remove('currentPage');
        }
        window.lastPageNumber = newPageNumber;
        window.currentPage.classList.add('currentPage');
        window.currentPage.parentNode.classList.add('currentSection');

        let offset = 10;
        let scrollTarget;
        if (newPageNumber === 0) {
            scrollTarget = window.titleElement;
        } else if (window.currentPage.parentNode.scrollHeight < (window.innerHeight * 0.8)) {
            scrollTarget = window.currentPage.parentNode;
        } else {
            scrollTarget = window.currentPage;
            if (window.currentPage.parentNode.getElementsByClassName('page')[0] !== window.currentPage) {
                offset = window.innerHeight * 0.2;
            }
        }

        let duration = isVisible(window.currentPage) ? 2000 : 300;
        scroll(scrollTarget, offset, duration);
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
    let elemTop = el.getBoundingClientRect().top;
    let elemBottom = el.getBoundingClientRect().bottom;
    return (elemTop >= 0) && (elemBottom <= window.innerHeight);
}