import { Location } from '@angular/common';
import { Injectable } from '@angular/core';

import { App } from '../app/app.const';

const SUPPORTED_LOCALE = [
  { id: 'ja', ja: '日本語', en: 'Japanese' },
  { id: 'en', ja: 'English', en: 'English' },
];

export class Locale {
  private static _id: string = undefined;

  static set(localeId: string) {
    if (window) {
      window.localStorage.setItem(App.Storage.LOCALE_ID, Locale.toSupported(localeId));
      window.location.reload();
    }
  }

  static get(): string {
    if (!Locale._id) {
      let id = window ? window.localStorage.getItem(App.Storage.LOCALE_ID) : undefined;
      if (!id) {
        id = window && window.navigator.language ? window.navigator.language.substr(0, 2) : undefined;
      }
      Locale._id = Locale.toSupported(id);
    }
    return Locale._id;
  }

  static getSupportedLocales(): Array<any> {
    return SUPPORTED_LOCALE.map(l => {
      return { 'id': l.id, 'name': l[this._id] };
    });
  }

  static getSupportedLocaleName(id: string, locale: string): string {
    let localeObj = SUPPORTED_LOCALE.find(l => l.id === id);
    return localeObj ? localeObj[locale] : undefined;
  }

  private static toSupported(localeId): string {
    if (SUPPORTED_LOCALE.some(l => l.id === localeId)) {
      return localeId;
    } else {
      return SUPPORTED_LOCALE[0].id;
    }
  }
}

