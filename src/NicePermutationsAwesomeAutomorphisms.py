from itertools import permutations
import numpy as np
from time import time

size = int(input("Anzahl an Knoten im Graphen:\n"))
matrix = np.zeros((size, size), dtype=int)

while True:
    s = input("Knotenpaar (Kante von nach) oder nichts eingeben:\n")
    if s == '':
        break
    v1, v2 = map(int, s.split())
    matrix[v1 - 1][v2 - 1] = 1
    matrix[v2 - 1][v1 - 1] = 1

begin = time()
perm = list(permutations([i for i in range(1, size + 1)]))  # all possible permutations
l = len(perm)
print(l, " Permutationen werden geprüft...")
RES = ["Id"]
c = 0
for p in perm:
    res = []
    matrix2 = matrix.copy()
    for i in range(size):
        a = p[i] - 1
        if a > i:
            matrix2[:, [a, i]] = matrix2[:, [i, a]]  # swap columns
            matrix2[[a, i]] = matrix2[[i, a]]  # swap lines
            if (i + 1, p[i]) not in res:
                res.append((i + 1, p[i]))
    if np.array_equal(matrix, matrix2):
        if len(res) != 0 and res not in RES:
            RES.append(res)
    if (l // 10) > 0 and c % (l // 10) == 0:
        print(str(int((c / (l // 10)) * 10)) + '% ', end='')
    c += 1

for i in range(len(RES)):
    j = 0
    while j < len(RES[i]) - 1:
        if RES[i][j][-1] == RES[i][j + 1][0]:  # Example: [(2, 3), (3, 4)] = [(2, 3, 4)]
            RES[i][j] = RES[i][j] + RES[i][j + 1][1:]  # "merge" (2, 3) and (3, 4) to (2, 3, 4)
            RES[i].remove(RES[i][j + 1])  # remove (3, 4)
            j -= 1
        j += 1

print("100%")
print("Automorphismen: ")
for i in RES:
    print(i)
end = time()
print("Erledigt in ", end - begin, 's')