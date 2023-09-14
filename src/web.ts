import { WebPlugin } from '@capacitor/core';

import type { SumUpOCPlugin } from './definitions';

export class SumUpOCWeb extends WebPlugin implements SumUpOCPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
