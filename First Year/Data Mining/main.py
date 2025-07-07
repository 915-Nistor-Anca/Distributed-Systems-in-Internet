from k_nearest_neighbors import heart_disease_prediction_knn
from logistic_regression import heart_disease_prediction_lr
from read_metrics import comparison
from support_vector_machine import heart_disease_prediction_svm

heart_disease_prediction_lr()
heart_disease_prediction_svm()
heart_disease_prediction_knn()

comparison()