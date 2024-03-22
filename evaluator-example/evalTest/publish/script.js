document.addEventListener('DOMContentLoaded', function() {

    const container = document.getElementById('test-container');
    const summaryDiv = document.createElement('div');
    summaryDiv.classList.add('test-summary');

    testData.results.forEach(block => {
        const blockDiv = document.createElement('div');
        blockDiv.classList.add('test-block');

        const title = document.createElement('h2');
        title.classList.add('test-title');
        title.textContent = block.description;
        blockDiv.appendChild(title);

        block.tests.forEach(test => {
            const inputDiv = document.createElement('div');
            inputDiv.classList.add(test.assert ? 'input-passed' : 'input-failed');
            inputDiv.textContent = 'Input: ' + test.input;
            blockDiv.appendChild(inputDiv);

            const outputDiv = document.createElement('div');
            outputDiv.classList.add('output');
            outputDiv.textContent = 'Output: ' + test.output;
            outputDiv.addEventListener('click', function() {
                this.classList.toggle('expanded');
            });
            blockDiv.appendChild(outputDiv);

            const scoreDiv = document.createElement('div');
            scoreDiv.classList.add('score', test.assert ? 'score-passed' : 'score-failed');
            scoreDiv.textContent = 'Score: ' + test.score.toFixed(3);
            blockDiv.appendChild(scoreDiv);
        });

        const avgScoreDiv = document.createElement('div');
        avgScoreDiv.classList.add('avg-score');
        avgScoreDiv.textContent = 'Average Score: ' + block.avg.toFixed(3);
        blockDiv.appendChild(avgScoreDiv);

        const testInfoDiv = document.createElement('div');
        testInfoDiv.classList.add('test-info');
        testInfoDiv.innerHTML = `
            Tests Passed: ${block.tests_successful} <br>
            Tests Failed: ${block.tests_failures} <br>
            Success Rate: ${block.success_rate.toFixed(2)}%
        `;
        blockDiv.appendChild(testInfoDiv);

        container.appendChild(blockDiv);

        summaryDiv.innerHTML += `
            <h3>${block.description}</h3>
            Average Score: ${block.avg.toFixed(3)} <br>
            Success Rate: ${block.success_rate.toFixed(2)}% <br><br>
        `;
    });

    container.appendChild(summaryDiv);
});
