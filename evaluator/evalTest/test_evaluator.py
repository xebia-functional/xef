from deepeval.metrics.answer_relevancy import AnswerRelevancyMetric
from deepeval.metrics.factual_consistency import FactualConsistencyMetric
from deepeval.test_case import LLMTestCase
from deepeval.evaluator import execute_test
import json

f = open('data.json')
data = json.load(f)

appDescription = data['description']

outputs = data['outputs_description']

numberOfOutputs = len(outputs)
minimumScore = float(data['minimum_score'])
metric = data['metric']

print()
print()
print(appDescription)
print("================")
print()
print(f"Using {metric} metric with {numberOfOutputs} different outputs ({minimumScore} minimum score)")

currentOutput = 0

metricObj = FactualConsistencyMetric(minimum_score=minimumScore)

if metric == "AnswerRelevancyMetric":
    metricObj = AnswerRelevancyMetric(minimum_score=minimumScore)

jsonResponse = {
    "description": appDescription,
}

jsonItemResultResponses = []

for x in range(numberOfOutputs):
    jsonItemResponse = {
        "description": outputs[x],

    }
    cases = []
    for item in data['items']:
        context = []
        if "context" in item:
            context = item['context']
        cases.append(LLMTestCase(input=item['input'], actual_output=item['actual_outputs'][x], context=context))

    print()
    results = execute_test(cases, [metricObj])
    print(f"Results: {outputs[x]}:")
    totalScore = 0

    jsonResultResponses = []

    for r in results:
        jsonResultResponse = {
            "input": r.input,
            "output": r.actual_output,
            "score": float(r.metrics[0].score)
        }
        jsonResultResponses.append(jsonResultResponse)
        totalScore += r.metrics[0].score
        print(f"- {r.input} -> {r.metrics[0].score}")
    avg = totalScore / len(results)
    jsonItemResponse["tests"] = jsonResultResponses
    jsonItemResponse["avg"] = avg
    jsonItemResultResponses.append(jsonItemResponse)
    print()
    print(f"Average: {avg}:")
    print()

jsonResponse["results"] = jsonItemResultResponses

with open("results.json", "w") as outfile:
    json.dump(jsonResponse, outfile)

print()

f.close()
