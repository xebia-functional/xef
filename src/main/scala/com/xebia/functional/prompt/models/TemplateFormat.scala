package com.xebia.functional.prompt.models

enum TemplateFormat(val name: String):
  case Jinja2 extends TemplateFormat("jinja2")
  case FString extends TemplateFormat("f-string")
