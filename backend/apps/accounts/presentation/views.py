from rest_framework import status
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView

from .serializers import RegisterCustomerSerializer


class RegisterCustomerView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = RegisterCustomerSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()

        return Response(
            {
                "id": user.id,
                "email": user.email,
                "first_name": user.first_name,
                "last_name": user.last_name,
                "role": user.role,
            },
            status=status.HTTP_201_CREATED,
        )
