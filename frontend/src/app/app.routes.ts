import { Routes } from '@angular/router';

import { AutenticadoGuard } from './guards/autenticado.guard';
import { EmpresaGuard } from './guards/empresa.guard';
import { EmpresaVerificacaoGuard } from './guards/empresa-verificacao.guard';
import { ExtratoGuard } from './guards/extrato.guard';

export const routes: Routes = [
  {
    path: '',
    title: 'KarvCoins',
    loadComponent: () =>
      import('./pages/landing-page/landing-page').then((component) => component.LandingPage),
  },
  {
    path: 'auth',
    title: 'Entrar | KarvCoins',
    loadComponent: () => import('./pages/auth/auth').then((component) => component.AuthPage),
  },
  {
    path: 'painel',
    title: 'Painel | KarvCoins',
    canActivate: [AutenticadoGuard],
    loadComponent: () =>
      import('./pages/home/home-page').then((component) => component.HomePage),
  },
  {
    path: 'extrato',
    title: 'Extrato | KarvCoins',
    canActivate: [ExtratoGuard],
    loadComponent: () =>
      import('./pages/extrato/extrato-page').then((component) => component.ExtratoPage),
  },
  {
    path: 'beneficios',
    title: 'Gerenciar Benefícios | KarvCoins',
    canMatch: [EmpresaGuard],
    loadComponent: () =>
      import('./pages/empresas/beneficios/empresa-beneficios-page').then(
        (component) => component.EmpresaBeneficiosPage,
      ),
  },
  {
    path: 'resgates/verificar/:codigoCupom',
    title: 'Verificar Resgate | KarvCoins',
    canActivate: [EmpresaVerificacaoGuard],
    loadComponent: () =>
      import('./pages/resgates/verificar/resgate-verificacao-page').then(
        (component) => component.ResgateVerificacaoPage,
      ),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
