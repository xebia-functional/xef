document.addEventListener('DOMContentLoaded', function() {
    fetch('results.json')
        .then(response => response.json())
        .then(data => displayResults(data));
});

function displayResults(data) {
    const container = document.getElementById('results-container');
    const comparisonDiv = document.createElement('div');
    comparisonDiv.className = 'comparison-block';

    data.results.forEach(block => {
        const blockDiv = document.createElement('div');
        blockDiv.className = 'test-block';

        const blockTitle = document.createElement('h2');
        blockTitle.textContent = block.description;
        blockDiv.appendChild(blockTitle);

        block.tests.forEach(test => {
            const testDiv = document.createElement('div');
            testDiv.className = 'test';

            const inputP = document.createElement('p');
            inputP.className = 'input';
            inputP.textContent = `Input: ${test.input}`;
            testDiv.appendChild(inputP);

            const outputP = document.createElement('p');
            outputP.className = 'output';
            outputP.textContent = `Output: ${test.output}`;
            outputP.addEventListener('click', () => outputP.classList.toggle('expanded'));
            testDiv.appendChild(outputP);

            const scoreP = document.createElement('p');
            scoreP.className = 'score';
            scoreP.textContent = `Score: ${test.score}`;
            testDiv.appendChild(scoreP);

            blockDiv.appendChild(testDiv);
        });

        const avgScore = document.createElement('p');
        avgScore.className = 'avg-score';
        avgScore.textContent = `Average Score: ${block.avg}`;
        blockDiv.appendChild(avgScore);

        container.appendChild(blockDiv);

        const comparisonP = document.createElement('p');
        comparisonP.textContent = `${block.description} -> ${block.avg}`;
        comparisonDiv.appendChild(comparisonP);
    });

    container.appendChild(comparisonDiv);
}
