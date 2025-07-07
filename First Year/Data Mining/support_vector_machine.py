import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
import seaborn as sns
from sklearn.metrics import confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


def svm_loss(w, b, X, Y, reg_param=0.01):
    """
    Compute the loss and gradients for SVM (hinge loss)
    """
    m = X.shape[1]
    # Hinge loss: 1 - y_i * (w^T * x_i + b)
    scores = np.dot(w.T, X) + b
    margin = Y * scores
    loss = np.sum(np.maximum(0, 1 - margin)) / m + reg_param * np.dot(w.T, w) / 2
    dw = -np.dot(X, (Y * (margin < 1)).T) / m + reg_param * w
    db = -np.sum(Y * (margin < 1)) / m
    return loss, dw, db


def initialize_parameters(dim):
    """
    Initialize the parameters w and b to zeros
    """
    w = np.zeros((dim, 1))
    b = 0
    return w, b


def optimize(w, b, X, Y, num_iterations=2000, learning_rate=0.01, reg_param=0.01):
    """
    Optimize w and b using gradient descent to minimize the SVM loss
    """
    costs = []
    for i in range(num_iterations):
        loss, dw, db = svm_loss(w, b, X, Y, reg_param)
        w -= learning_rate * dw
        b -= learning_rate * db

        if i % 100 == 0:
            costs.append(loss)

    params = {"w": w, "b": b}
    return params, costs


def predict(w, b, X):
    """
    Predict the class labels for the input data
    """
    scores = np.dot(w.T, X) + b
    predictions = np.sign(scores)
    return predictions


from sklearn.metrics import classification_report, roc_auc_score


def heart_disease_prediction_svm():
    df = pd.read_csv("heart_disease_uci.csv")

    df = df.dropna(subset=['age', 'trestbps', 'chol', 'thalch', 'oldpeak', 'num'])

    df['num'] = df['num'].apply(lambda x: 1 if x > 0 else -1)

    features = ['age', 'trestbps', 'chol', 'thalch', 'oldpeak']
    X = df[features].values.T
    y = df['num'].values.reshape(1, -1)

    X_train, X_test, y_train, y_test = train_test_split(X.T, y.T, test_size=0.2, random_state=42)

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    w, b = initialize_parameters(X_train_scaled.shape[1])

    params, costs = optimize(w, b, X_train_scaled.T, y_train.T, num_iterations=2000, learning_rate=0.01)

    w = params["w"]
    b = params["b"]

    y_pred_train = predict(w, b, X_train_scaled.T)
    y_pred_test = predict(w, b, X_test_scaled.T)

    report = classification_report(y_test.T.flatten(), y_pred_test.flatten(), output_dict=True)

    precision = report['1']['precision']
    recall = report['1']['recall']
    f1_score = report['1']['f1-score']
    accuracy = report['accuracy']

    y_prob_test = np.dot(w.T, X_test_scaled.T) + b
    roc_auc = roc_auc_score(y_test.T.flatten(), y_prob_test.T.flatten())

    with open("results_svm.txt", "w") as f:
        f.write(f"Precision: {precision:.4f}\n")
        f.write(f"Recall: {recall:.4f}\n")
        f.write(f"F1 Score: {f1_score:.4f}\n")
        f.write(f"Accuracy: {accuracy:.4f}\n")
        f.write(f"ROC-AUC Score: {roc_auc:.4f}")

    cm = confusion_matrix(y_test.T.flatten(), y_pred_test.flatten())
    plt.figure(figsize=(6, 4))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=["Negative", "Positive"],
                yticklabels=["Negative", "Positive"])
    plt.title("SVM - Confusion Matrix")
    plt.xlabel("Predicted")
    plt.ylabel("Actual")
    plt.show()
