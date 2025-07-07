import matplotlib.pyplot as plt
import numpy as np

def comparison():
    def read_metrics(file_path):
        metrics = {}
        with open(file_path, "r") as f:
            lines = f.readlines()
            for line in lines:
                if "Precision" in line:
                    metrics["Precision"] = float(line.split(":")[1].strip())
                elif "Recall" in line:
                    metrics["Recall"] = float(line.split(":")[1].strip())
                elif "F1 Score" in line:
                    metrics["F1 Score"] = float(line.split(":")[1].strip())
                elif "Accuracy" in line:
                    metrics["Accuracy"] = float(line.split(":")[1].strip())
                elif "ROC-AUC Score" in line:
                    metrics["ROC-AUC Score"] = float(line.split(":")[1].strip())
        return metrics

    lr_metrics = read_metrics("results_lr.txt")
    svm_metrics = read_metrics("results_svm.txt")
    knn_metrics = read_metrics("results_knn.txt")

    metrics = {
        "Precision": [lr_metrics["Precision"], svm_metrics["Precision"], knn_metrics["Precision"]],
        "Recall": [lr_metrics["Recall"], svm_metrics["Recall"], knn_metrics["Recall"]],
        "F1 Score": [lr_metrics["F1 Score"], svm_metrics["F1 Score"], knn_metrics["F1 Score"]],
        "Accuracy": [lr_metrics["Accuracy"], svm_metrics["Accuracy"], knn_metrics["Accuracy"]],
        "ROC-AUC Score": [lr_metrics["ROC-AUC Score"], svm_metrics["ROC-AUC Score"], knn_metrics["ROC-AUC Score"]]
    }

    models = ["Logistic Regression", "SVM", "KNN"]

    bar_width = 0.15
    index = np.arange(len(models))

    fig, ax = plt.subplots(figsize=(10, 6))

    for i, (metric, values) in enumerate(metrics.items()):
        ax.bar(index + i * bar_width, values, bar_width, label=metric)

    ax.set_xlabel("Models")
    ax.set_ylabel("Scores")
    ax.set_title("Model Comparison - Precision, Recall, F1 Score, Accuracy, ROC-AUC")
    ax.set_xticks(index + bar_width * 2)  # Adjusting the x-tick positions
    ax.set_xticklabels(models)
    ax.legend()

    plt.tight_layout()
    plt.show()
