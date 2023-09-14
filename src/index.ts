import { registerPlugin } from '@capacitor/core';

import type { SumUpOCPlugin } from './definitions';

const SumUpOC = registerPlugin<SumUpOCPlugin>('SumUpOC', {
  web: () => import('./web').then(m => new m.SumUpOCWeb()),
});

export * from './definitions';
export { SumUpOC };
