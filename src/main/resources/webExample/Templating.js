const Templating = {

    HtmlTemplateElement: class HtmlTemplateElement {
        /**
         * @param {HTMLElement} element
         */
        constructor(element) {
            this.element = element;
        }

        /**
         * @param {string | HTMLElement | string[] | HTMLElement[]} children an html-string, html-element or array of either to add as the children of this
         * element.
         * @return {Templating.HtmlTemplateElement}
         */
        child(children) {
            if (!children) {
                return this;
            }

            const type = typeof (children);
            if (type === 'string') {
                this.element.appendChild(Templating.html(children).element);
            } else if (type === 'object') {
                if (Array.isArray(children)) {
                    if (children.length <= 0) {
                        return this;
                    }

                    if (typeof (children[0]) === 'string') {
                        for (let child of children) {
                            this.element.appendChild(Templating.html(child).element);
                        }
                    } else {
                        for (let child of children) {
                            this.element.appendChild(child);
                        }
                    }
                } else {
                    // assuming HTMLElement
                    this.element.appendChild(children);
                }
            }

            return this;
        }
    },

    /**
     * @param {string} htmlString representing a single element
     * @return {Templating.HtmlTemplateElement}
     */
    html: function html(htmlString) {
        let template = document.createElement('template');
        template.innerHTML = htmlString.trim();
        // noinspection JSCheckFunctionSignatures
        return new Templating.HtmlTemplateElement(template.content.firstChild);
    }
}