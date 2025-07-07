import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
import seaborn as sns
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


def sigmoid(z):
    return 1 / (1 + np.exp(-np.clip(z, -500, 500)))

def initialize_parameters(dim):
    w = np.zeros((dim, 1))
    b = 0
    return w, b

def propagate(w, b, X, Y):
    m = X.shape[1]
    A = sigmoid(np.dot(w.T, X) + b)
    A = np.clip(A, 1e-10, 1 - 1e-10)
    cost = -1/m * np.sum(Y * np.log(A) + (1 - Y) * np.log(1 - A))
    dw = 1/m * np.dot(X, (A - Y).T)
    db = 1/m * np.sum(A - Y)
    grads = {"dw": dw, "db": db}
    return grads, cost

def optimize(w, b, X, Y, num_iterations, learning_rate):
    costs = []

    for i in range(num_iterations):
        grads, cost = propagate(w, b, X, Y)

        dw = grads["dw"]
        db = grads["db"]

        w -= learning_rate * dw
        b -= learning_rate * db

        if i % 100 == 0:
            costs.append(cost)

    params = {"w": w, "b": b}
    return params, costs

def predict(w, b, X):
    A = sigmoid(np.dot(w.T, X) + b)
    predictions = (A > 0.5).astype(int)
    return predictions

def confusion_matrix_custom(y_true, y_pred):
    tp = np.sum((y_true == 1) & (y_pred == 1))
    tn = np.sum((y_true == 0) & (y_pred == 0))
    fp = np.sum((y_true == 0) & (y_pred == 1))
    fn = np.sum((y_true == 1) & (y_pred == 0))

    return np.array([[tn, fp],
                     [fn, tp]])

def plot_confusion_matrix(y_true, y_pred):
    cm = confusion_matrix_custom(y_true, y_pred)
    plt.figure(figsize=(6, 4))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=["Negative", "Positive"],
                yticklabels=["Negative", "Positive"])
    plt.title("Logistic Regression - Confusion Matrix")
    plt.xlabel("Predicted")
    plt.ylabel("Actual")
    plt.show()

def classification_report_custom(y_true, y_pred):
    tp = np.sum((y_true == 1) & (y_pred == 1))
    tn = np.sum((y_true == 0) & (y_pred == 0))
    fp = np.sum((y_true == 0) & (y_pred == 1))
    fn = np.sum((y_true == 1) & (y_pred == 0))

    precision = tp / (tp + fp) if tp + fp > 0 else 0
    recall = tp / (tp + fn) if tp + fn > 0 else 0
    f1_score = 2 * precision * recall / (precision + recall) if precision + recall > 0 else 0
    accuracy = (tp + tn) / len(y_true)

    return {
        "precision": precision,
        "recall": recall,
        "f1_score": f1_score,
        "accuracy": accuracy
    }

def model(X_train, Y_train, X_test, Y_test, num_iterations=2000, learning_rate=0.01):
    w, b = initialize_parameters(X_train.shape[0])
    params, costs = optimize(w, b, X_train, Y_train, num_iterations, learning_rate)

    w = params["w"]
    b = params["b"]

    Y_pred_train = predict(w, b, X_train)
    Y_pred_test = predict(w, b, X_test)

    report = classification_report_custom(Y_test.flatten(), Y_pred_test.flatten())

    A_test = sigmoid(np.dot(w.T, X_test) + b)
    roc_auc = roc_auc_score(Y_test.flatten(), A_test.flatten())
    plot_confusion_matrix(Y_test.flatten(), Y_pred_test.flatten())


    with open("results_lr.txt", "w") as f:
        f.write(f"Precision: {report['precision']:.4f}\n")
        f.write(f"Recall: {report['recall']:.4f}\n")
        f.write(f"F1 Score: {report['f1_score']:.4f}\n")
        f.write(f"Accuracy: {report['accuracy']:.4f}\n")
        f.write(f"ROC-AUC Score: {roc_auc:.4f}")

    return params, costs

def heart_disease_prediction_lr():
    df = pd.read_csv("heart_disease_uci.csv")
    df = df.dropna(subset=['age', 'trestbps', 'chol', 'thalch', 'oldpeak', 'num'])
    df['num'] = df['num'].apply(lambda x: 1 if x > 0 else 0)

    features = ['age', 'trestbps', 'chol', 'thalch', 'oldpeak']
    X = df[features].values.T
    y = df['num'].values.reshape(1, -1)

    X_train, X_test, y_train, y_test = train_test_split(X.T, y.T, test_size=0.2, random_state=42)

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    model(X_train_scaled.T, y_train.T, X_test_scaled.T, y_test.T)