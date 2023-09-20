import { WebPlugin } from '@capacitor/core';

import type { SumUpOCPlugin } from './definitions';

export class SumUpOCWeb extends WebPlugin implements SumUpOCPlugin {
  constructor() {
    super({
      name: 'SumUpOC',
      platforms: ['web'],
    });
  }
  async login(options: { affiliateKey: string; accessToken?: string; }): Promise<{ code: number; message: string; }> {
    console.log('login', options);
    return { code: 1, message: 'Not implemented on web.' };
  }
  async checkout(options: { total: number; currency: string; title?: string; receiptEmail?: string;
     receiptSMS?: string; additionalInfo?: { [key: string]: string; };
     foreignTransactionId?: string; skipSuccessScreen?: boolean; }): Promise<{ code: number; message: string; }> {
    console.log('checkout', options);
    return { code: 1, message: 'Not implemented on web.' };
  }
  async logout() {
    console.log('logout');
    return { code: 1, message: 'Not implemented on web.' };
  }
}
