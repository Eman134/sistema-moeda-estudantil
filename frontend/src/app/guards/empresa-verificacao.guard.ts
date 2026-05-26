import { inject, Injectable } from '@angular/core';
import { CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';

import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class EmpresaVerificacaoGuard implements CanActivate {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  canActivate(_route: unknown, state: RouterStateSnapshot): boolean | UrlTree {
    if (!this.authService.estaAutenticado()) {
      return this.criarUrlLogin(state.url);
    }

    if (!this.authService.usuarioTemPerfil('EMPRESA')) {
      this.authService.sair();
      return this.criarUrlLogin(state.url);
    }

    return true;
  }

  private criarUrlLogin(returnUrl: string): UrlTree {
    return this.router.createUrlTree(['/auth'], {
      queryParams: { returnUrl },
    });
  }
}
