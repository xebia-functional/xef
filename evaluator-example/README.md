## Evaluator Example

This is an example of how to use the evaluator. The evaluator is a tool that 
can be used to evaluate the performance of a model on a dataset. 
It can be used to evaluate the performance of a model on a dataset, 
or to compare the performance of multiple models on a dataset.

This module contains an example that you only have to copy to your project and 
adapt to your needs. 

### Pre-requisites

You need to have the following installed:

- [Install virtualenv](https://virtualenv.pypa.io/en/latest/installation.html)

```shell
pipx install virtualenv
```

- [Install Poetry](https://python-poetry.org/docs/#installing-with-pipx)

**Note**: Poetry requires **Python 3.10.x**. You can use [pyenv](https://github.com/pyenv/pyenv) to
easily switch between multiple versions of Python.

Then, you can configure your local environment with virtualenv

```bash
# Change with your Python patch version 
virtualenv venv --python=python3.10.0
source venv/bin/activate
```

Once you have Poetry installed, you can install the dependencies. You have to
move to `evalTest` folder and execute the following command:

```bash
poetry install
```

### Usage

To try this example, you can run the following command:

```bash
./gradlew evaluator
```

After running the command, you will have the results saved 
in a web, that you can see opening the file: `evalTest/index.html`
