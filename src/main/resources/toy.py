import sys
import numpy as np
from sklearn.datasets import make_moons, make_circles, make_classification

if __name__ == '__main__':
    if sys.argv[1] == 'linear':
        X, y = make_classification(n_features=2, n_redundant=0, n_clusters_per_class=1, random_state=1)
        X[:, 0] += float(sys.argv[2]) * np.random.RandomState().uniform(size=y.size)
    elif sys.argv[1] == 'moon':
        X, y = make_moons(noise=float(sys.argv[2]), random_state=0)
    elif sys.argv[1] == 'circle':
        X, y = make_circles(noise=float(sys.argv[2]), random_state=1, factor=0.5)
    else:
        sys.exit(1)
    y[y == 0] = -1
    np.savetxt(sys.argv[3], np.c_[X, y], fmt='%.2f,%.2f,%.0f', encoding='utf-8')
    sys.exit(0)
