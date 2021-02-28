from tabulate import tabulate

a, b = map(int, input("Geben Sie a und b an").split())
if a > b:
    a, b = b, a
table = []
while a != 0:
    k = b // a
    table.append([a, b, k, 0, 0])
    ca = a
    a = b - k * a
    b = ca
table.append([a, b, '-', 0, 1])

for i in range(len(table) - 2, -1, -1):
    table[i][4] = table[i + 1][3]  # β = α'
    table[i][3] = -table[i][2] * table[i + 1][3] + table[i + 1][4]  # α = -k * α' + β'
print(tabulate(table, headers=['a', 'b', 'k', 'α', 'β']))