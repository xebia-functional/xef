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

for x in range(numberOfOutputs):
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
    for r in results:
        # print_test_result(r)
        totalScore += r.metrics[0].score
        print(f"- -> {r.metrics[0].score}")
    avg = totalScore / len(results)
    print()
    print(f"Average: {avg}:")
    print()

print()

f.close()
