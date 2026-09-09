from django.contrib.auth.base_user import BaseUserManager
from django.contrib.auth.models import AbstractUser
from django.db import models


class UserRole(models.TextChoices):
    CUSTOMER = "customer", "Cliente"
    STAFF = "staff", "Equipe"
    OWNER = "owner", "Proprietário"


class UserManager(BaseUserManager):
    def create_user(self, email: str, password: str | None = None, **extra_fields):
        if not email:
            raise ValueError("O e-mail é obrigatório.")

        user = self.model(email=self.normalize_email(email), **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, email: str, password: str | None = None, **extra_fields):
        extra_fields.setdefault("is_staff", True)
        extra_fields.setdefault("is_superuser", True)
        extra_fields.setdefault("role", UserRole.OWNER)

        if extra_fields.get("is_staff") is not True:
            raise ValueError("Superusuários precisam de is_staff=True.")
        if extra_fields.get("is_superuser") is not True:
            raise ValueError("Superusuários precisam de is_superuser=True.")

        return self.create_user(email, password, **extra_fields)


class User(AbstractUser):
    """Usuário único para a loja, com acesso definido por papel no servidor."""

    username = None
    email = models.EmailField("e-mail", unique=True)
    role = models.CharField(
        "papel",
        max_length=20,
        choices=UserRole.choices,
        default=UserRole.CUSTOMER,
    )

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS: list[str] = []
    objects = UserManager()

    class Meta:
        verbose_name = "usuário"
        verbose_name_plural = "usuários"

    @property
    def can_access_backoffice(self) -> bool:
        return self.role in {UserRole.STAFF, UserRole.OWNER}
