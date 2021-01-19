
// https://www.dannyguo.com/blog/how-to-add-copy-to-clipboard-buttons-to-code-blocks-in-hugo/

function addCopyButtons(clipboard) {
    document.querySelectorAll('pre > code').forEach(function (codeBlock) {
        var button = document.createElement('button');
        button.className = 'copy-code-button';
        button.type = 'button';
        button.innerText = 'Copy';
        button.addEventListener('click', function () {
            if (pre.parentNode.classList.contains('highlight')) {
                 var code = codeBlock.innerText.trim(); // chroma with language
             } else {
                 var code = codeBlock.innerText.trim().replace("Copy\n", ""); // raw code
             };

            clipboard.writeText(code).then(function () {
                /* Chrome doesn't seem to blur automatically,
                   leaving the button in a focused state. */
                button.blur();

                button.innerText = 'Copied!';

                setTimeout(function () {
                    button.innerText = 'Copy';
                }, 700);
            }, function (error) {
                button.innerText = 'Error';
            });
        });
        var pre = codeBlock.parentNode;
        if (pre.parentNode.classList.contains('highlight')) {
            var highlight = pre.parentNode;
            if (highlight.firstChild.nodeName === 'PRE') {
                // <div class="highligh"><pre ... no space between ensures copy button is added!
                highlight.firstChild.insertBefore(button, highlight.firstChild.firstChild);
            } else {
                // <div class="highligh"> <pre ... space between avoids copy button being added!
            }
        } else {
            pre.firstChild.insertBefore(button, pre.firstChild.firstChild);
        }
    });
}

if (navigator && navigator.clipboard) {
    addCopyButtons(navigator.clipboard);
} else {
    var script = document.createElement('script');
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/clipboard-polyfill/2.7.0/clipboard-polyfill.promise.js';
    script.integrity = 'sha256-waClS2re9NUbXRsryKoof+F9qc1gjjIhc2eT7ZbIv94=';
    script.crossOrigin = 'anonymous';
    script.onload = function() {
        addCopyButtons(clipboard);
    };
    document.body.appendChild(script);
}
