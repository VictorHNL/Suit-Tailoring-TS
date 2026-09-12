from django.urls import path

from .views import RegisterCustomerView

app_name = "accounts"

urlpatterns = [
    path("register/", RegisterCustomerView.as_view(), name="register"),
]
