import {OpenAI} from "openai/index";
import {Settings} from "@/state/Settings";

export function openai (settings: Settings): OpenAI {
  if (!settings.apiKey) throw 'API key not set';
  return new OpenAI({
    // TODO: remove this when the key is the user token used for client auth
    dangerouslyAllowBrowser: true,
    apiKey: settings.apiKey, // defaults to process.env["OPENAI_API_KEY"]
  });
}