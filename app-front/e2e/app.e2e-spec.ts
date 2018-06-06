import { hogeMarketPage } from './app.po';

describe('hoge-market App', function() {
  let page: hogeMarketPage;

  beforeEach(() => {
    page = new hogeMarketPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
