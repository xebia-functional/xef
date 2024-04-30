package com.xebia.functional.xef.evaluator.models

@JvmInline
value class Html(val value: String) {

  companion object {

    // language=javascript
    private val jsContent =
      """
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
                    const itemDescription = document.createElement('div');
                    itemDescription.textContent = 'Description: ' + test.description;
                    blockDiv.appendChild(itemDescription);
        
                    const context = document.createElement('div');
                    context.textContent = 'Context: ' + test.contextDescription;
                    blockDiv.appendChild(context);
        
                    const outputDiv = document.createElement('pre');
                    outputDiv.classList.add('output');
                    outputDiv.innerText = 'Output: ' + test.output;
                    blockDiv.appendChild(outputDiv);
                    
                    const usageDiv = document.createElement('pre');
                    usageDiv.classList.add('output');
                    usageDiv.innerText = 'Usage: \n Completion Tokens: ' + test.usage?.completionTokens + '\n Prompt Tokens: ' + test.usage?.promptTokens + '\n Total Tokens: ' + test.usage?.totalTokens;
                    blockDiv.appendChild(usageDiv);
        
                    const result = document.createElement('div');
                    result.classList.add('score', test.success ? 'score-passed' : 'score-failed');
                    result.textContent = 'Result: ' + test.result;
                    blockDiv.appendChild(result);
        
                    blockDiv.appendChild(document.createElement('br'));
                });
                container.appendChild(blockDiv);
            });
        
        });
        """
        .trimIndent()

    // language=css
    private val cssContent =
      """
          body {
              font-family: Arial, sans-serif;
              margin: 0;
              padding: 0;
              background-color: #f4f4f4;
          }

          #test-container {
              width: 80%;
              margin: 20px auto;
              padding: 15px;
              background-color: white;
              border-radius: 8px;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          }

          .test-block {
              margin-bottom: 20px;
              border-bottom: 1px solid #eee;
              padding-bottom: 20px;
          }

          .test-title {
              font-size: 1.2em;
              color: #333;
          }

          .input, .output {
              margin: 5px 0;
          }

          .input-passed {
              margin-top: 25px;
              color: green;
              font-weight: bold;
          }

          .input-failed {
              margin-top: 25px;
              color: red;
              font-weight: bold;
          }

          .output {
              color: #666;
              white-space: normal;
              overflow: hidden;
              text-overflow: ellipsis;
          }

          .score {
              font-weight: bold;
          }

          .score-passed {
              margin-bottom: 25px;
              color: #008000;
          }

          .score-failed {
              margin-bottom: 25px;
              color: red;
          }

          .avg-score, .test-info {
              font-size: 1.2em;
              color: #d35400;
              margin-top: 10px;
          }

          .test-summary {
              background-color: #e7e7e7;
              padding: 15px;
              margin-top: 20px;
              border-radius: 8px;
          }

          .test-summary h3 {
              font-size: 1.1em;
              color: #555;
              margin-top: 0;
          }

        """
        .trimIndent()

    fun get(contentJson: String, suiteName: String): Html {
      // language=html
      return Html(
        """
          <!DOCTYPE html>
          <html lang="es">
          <head>
              <meta charset="UTF-8">
              <title>Tests</title>
              <style>$cssContent</style>
              <script>
                $jsContent
                const testData = $contentJson;
                const suiteName = $suiteName
              </script>
          </head>
          <body>
          <div id="test-container"></div>
          </body>
          </html>
        """
          .trimIndent()
      )
    }
  }
}
