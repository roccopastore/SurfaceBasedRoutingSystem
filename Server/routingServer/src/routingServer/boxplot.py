import pandas as pd

df = pd.read_csv('/Users/roccopastore/Desktop/NavigationMap/diff.csv')

#create boxplot
boxplot = df.boxplot(figsize = (5,5))
