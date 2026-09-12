from django.test import TestCase

from apps.accounts.application.register_customer import (
    EmailAlreadyRegisteredError,
    register_customer,
)
from apps.accounts.models import UserRole


class RegisterCustomerTests(TestCase):
    def test_creates_a_customer_with_a_hashed_password(self):
        user = register_customer(
            email="cliente@exemplo.com",
            password="SenhaForte!123",
            first_name="Ana",
            last_name="Silva",
        )

        self.assertEqual(user.email, "cliente@exemplo.com")
        self.assertEqual(user.role, UserRole.CUSTOMER)
        self.assertTrue(user.check_password("SenhaForte!123"))

    def test_rejects_an_already_registered_email(self):
        register_customer(
            email="cliente@exemplo.com",
            password="SenhaForte!123",
        )

        with self.assertRaises(EmailAlreadyRegisteredError):
            register_customer(
                email="CLIENTE@EXEMPLO.COM",
                password="OutraSenhaForte!123",
            )
