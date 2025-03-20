from ast import Store
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import csv



dataset = pd.read_csv('/data/labelledData.csv')
X = dataset.iloc[:,[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,35]].values


labels = np.array(dataset['class'])

listOfFeatures = ['class', 'Timestamp', 'latitude', 'longitude']
features = dataset.drop(listOfFeatures, axis=1)

feature_list = list(features.columns)
features = np.array(features)


from sklearn.model_selection import train_test_split

train_features, test_features, train_labels, test_labels = train_test_split(features, labels, test_size = 0.25, random_state = 42)

from sklearn.ensemble import RandomForestClassifier
rf = RandomForestClassifier(n_estimators = 1000, random_state = 42)
rf.fit(features, labels)


		

import os, glob
import csv



with open('/data/newdata.csv', 'w' , newline='') as file:
	writer = csv.writer(file)
	writer.writerow(["Timestamp", "VarAcc", "VarLAcc", "VarGyr", "VarMagn", "AvgAcc", "AvgLAcc", "AvgGyr", "AvgMagn", "sdAcc", "sdLAcc", "sdGyr", "sdMagn", "MaxAcc", "MaxLAcc", "MaxGyr", "MaxMagn", "MinAcc", "MinLAcc", "MinGyr", "MinMagn", "RangeAcc", "RangeLAcc", "RangeGyr", "RangeMagn", "SkAcc", "SkLAcc", "SkGyr", "SkMagn", "KuAcc", "KuLAcc", "KuGyr", "KuMagn", "Latitude", "Longitude"])
	c = 0
	path = '/data/rawdata'
	for filename in glob.glob(os.path.join(path, '*')):
			with open(os.path.join(os.getcwd(), filename), 'r') as f:
				lines = f.readlines()
				for line in lines:
					arr = []
					if "Timestamp" not in line:
						x = line.split(",")
						x.pop(len(x)-1)
						writer.writerow(x)


from sklearn.preprocessing import StandardScaler



df = pd.read_csv('/data/newdata.csv')
X = df.iloc[:,[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32]].values


object = StandardScaler()
scale = object.fit_transform(X)

features = np.array(scale)

ypred = rf.predict(features)

with open('/data/labelledData.csv', 'a') as file:
	writer = csv.writer(file)
	with open('/data/newdata.csv', 'r') as f:
		lines = f.readlines()
		count4predictions = 0
		count4rows = 0
		for line in lines:
			if(count4rows != 0):
				toWrite = line.split(",")
				toWrite.append(str(ypred[count4predictions]))
				writer.writerow(toWrite)
				count4predictions+=1
			count4rows+=1
