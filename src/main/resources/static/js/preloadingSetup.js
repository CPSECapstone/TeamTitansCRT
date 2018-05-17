function visitCheck() {
    if (!localStorage.getItem("visitedLandingPage")) {
        window.location.href = '/';
    }
}

visitCheck();