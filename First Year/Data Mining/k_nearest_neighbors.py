import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from logistic_regression import confusion_matrix_custom

def euclidean_distance(X1, X2):
    return np.sqrt(np.sum((X1 - X2) ** 2, axis=1))

def knn_predict(X_train, y_train, X_test, k=3):
    y_pred = []

    for test_point in X_test:
        distances = euclidean_distance(X_train, test_point)

        k_indices = np.argsort(distances)[:k]

        k_nearest_labels = y_train[k_indices]

        unique, counts = np.unique(k_nearest_labels, return_counts=True)
        majority_vote = unique[np.argmax(counts)]

        y_pred.append(majority_vote)

    return np.array(y_pred)


def plot_confusion_matrix(y_true, y_pred):
    cm = confusion_matrix_custom(y_true, y_pred)
    plt.figure(figsize=(6, 4))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=["Negative", "Positive"],
                yticklabels=["Negative", "Positive"])
    plt.title("K-Nearest Neighbors - Confusion Matrix")
    plt.xlabel("Predicted")
    plt.ylabel("Actual")
    plt.show()


from sklearn.metrics import classification_report, roc_auc_score, confusion_matrix
import matplotlib.pyplot as plt
import seaborn as sns

def heart_disease_prediction_knn():
    df = pd.read_csv("heart_disease_uci.csv")
    df = df.dropna(subset=['age', 'trestbps', 'chol', 'thalch', 'oldpeak', 'num'])
    df['num'] = df['num'].apply(lambda x: 1 if x > 0 else 0)

    features = ['age', 'trestbps', 'chol', 'thalch', 'oldpeak']
    X = df[features].values
    y = df['num'].values

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    y_pred_train = knn_predict(X_train_scaled, y_train, X_train_scaled, k=3)
    y_pred_test = knn_predict(X_train_scaled, y_train, X_test_scaled, k=3)

    train_accuracy = 100 - np.mean(np.abs(y_pred_train - y_train)) * 100
    test_accuracy = 100 - np.mean(np.abs(y_pred_test - y_test)) * 100

    report = classification_report(y_test, y_pred_test, output_dict=True)

    precision = report['1']['precision']
    recall = report['1']['recall']
    f1_score = report['1']['f1-score']
    accuracy = report['accuracy']

    roc_auc = roc_auc_score(y_test, y_pred_test)

    with open("results_knn.txt", "w") as f:
        f.write(f"Precision: {precision:.4f}\n")
        f.write(f"Recall: {recall:.4f}\n")
        f.write(f"F1 Score: {f1_score:.4f}\n")
        f.write(f"Accuracy: {accuracy:.4f}\n")
        f.write(f"ROC-AUC Score: {roc_auc:.4f}")

    cm = confusion_matrix(y_test, y_pred_test)
    plt.figure(figsize=(6, 4))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=["Negative", "Positive"],
                yticklabels=["Negative", "Positive"])
    plt.title("KNN - Confusion Matrix")
    plt.xlabel("Predicted")
    plt.ylabel("Actual")
    plt.show()
