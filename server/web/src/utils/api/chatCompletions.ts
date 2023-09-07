import {
  defaultApiServer,
} from '@/utils/api';

import {OpenAI} from "openai/index";

export function openai (settings: Settings): OpenAI {
  if (!settings.apiKey) throw 'API key not set';
  return new OpenAI({
    baseURL: defaultApiServer,
    // TODO: remove this when the key is the user token used for client auth
    dangerouslyAllowBrowser: true,
    apiKey: settings.apiKey, // defaults to process.env["OPENAI_API_KEY"]
  });
}
