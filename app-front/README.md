# app-front

This project was generated with [angular-cli](https://github.com/angular/angular-cli).


## How to start develop

At first, run `npm install -g @angular/cli` & `npm install`.  

And you just have to  

1. `npm start`
2. Access to `https://localhost:4200/`

## Appendix

### `ng serve`

Launch development server via [angular-cli](https://github.com/angular/angular-cli).  
The app will automatically reload when you change any of the source files.

### Upgrade angular-cli

Global package:
1. `npm uninstall -g @angular/cli`
2. `npm cache verify`
3. `npm install -g @angular/cli@latest`

Local project package:
1. Remove `node_modules` dir.
2. `npm install`

## Further help

To get more help on the `angular-cli` use `ng help` or go check out the [Angular-CLI README](https://github.com/angular/angular-cli/blob/master/README.md).


## How to build per tenant

Each tenants have own CloudFront Distribution.  
`ng build --prod --aot --environment=eval --deploy-url https://<domain>/`