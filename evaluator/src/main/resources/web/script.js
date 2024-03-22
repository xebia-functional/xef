document.addEventListener('DOMContentLoaded', function() {

    const container = document.getElementById('test-container');

    const headerDiv = document.createElement('div');
    headerDiv.classList.add('test-block');

    const header = document.createElement('h1');
    header.classList.add('test-header');
    header.textContent = "Suite test";

    const suiteDescription = document.createElement('p');
    suiteDescription.textContent = 'Description: ' + testData.description;

    const model = document.createElement('p');
    model.textContent = 'Model: ' + testData.model;

    const metric = document.createElement('p');
    metric.textContent = 'Metric: ' + testData.metric;

    headerDiv.appendChild(header);
    headerDiv.appendChild(suiteDescription);
    headerDiv.appendChild(model);
    headerDiv.appendChild(metric);

    container.appendChild(headerDiv);

    testData.items.forEach(block => {
        const blockDiv = document.createElement('div');
        blockDiv.classList.add('test-block');

        const title = document.createElement('h2');
        title.classList.add('test-title');
        title.textContent = 'Input: ' + block.description;

        blockDiv.appendChild(title);

        block.items.forEach(test => {
            const context = document.createElement('div');
            context.textContent = 'Context: ' + test.contextDescription;
            blockDiv.appendChild(context);

            const outputDiv = document.createElement('pre');
            outputDiv.classList.add('output');
            outputDiv.innerText = 'Output: ' + test.output;
            outputDiv.addEventListener('click', function() {
                this.classList.toggle('expanded');
            });
            blockDiv.appendChild(outputDiv);

            const result = document.createElement('div');
            result.textContent = 'Result: ' + test.result;
            blockDiv.appendChild(result);

            blockDiv.appendChild(document.createElement('br'));
        });
        container.appendChild(blockDiv);
    });

});
