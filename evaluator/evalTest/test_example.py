import pytest
from deepeval.metrics.factual_consistency import FactualConsistencyMetric
from deepeval.test_case import LLMTestCase
from deepeval.evaluator import run_test
# from deepeval.run_test import run_test

input = "What if these shoes don't fit?"
context = ["All customers are eligible for a 30 day full refund at no extra cost."]

# Replace this with the actual output from your LLM application
actual_output = "We offer a 30-day full refund at no extra cost."

factual_consistency_metric = FactualConsistencyMetric(minimum_score=0.7)
test_case = LLMTestCase(input=input, actual_output=actual_output, context=context)

r = run_test(test_case, [factual_consistency_metric])
print(r)
