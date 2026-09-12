from django.core.exceptions import ValidationError as DjangoValidationError
from rest_framework import serializers

from apps.accounts.application.register_customer import (
    EmailAlreadyRegisteredError,
    register_customer,
)


class RegisterCustomerSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True, trim_whitespace=False)
    first_name = serializers.CharField(required=False, allow_blank=True, max_length=150)
    last_name = serializers.CharField(required=False, allow_blank=True, max_length=150)

    def create(self, validated_data):
        try:
            return register_customer(**validated_data)
        except EmailAlreadyRegisteredError as error:
            raise serializers.ValidationError(
                {"email": "Já existe uma conta com este e-mail."}
            ) from error
        except DjangoValidationError as error:
            raise serializers.ValidationError(
                {"password": error.messages}
            ) from error
