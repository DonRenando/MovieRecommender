import unittest
from random import randint
from funkload.FunkLoadTestCase import FunkLoadTestCase

class bench(FunkLoadTestCase):

    def setUp(self):
        self.server_url = self.conf_get('main', 'url')

    def test_movies(self):
        server_url = self.server_url
        id_user_start = self.conf_getInt('test_movies', 'id_user_start')
        id_user_end = self.conf_getInt('test_movies', 'id_user_end')
        for i in range(id_user_start,id_user_end+1):
            self.get(server_url+"/recommendations?user_id="+str(i), description='Get Movies')
    
    def test_ratings(self):
        server_url = self.server_url
        id_user_start = self.conf_getInt('test_ratings', 'id_user_start')
        id_user_end = self.conf_getInt('test_ratings', 'id_user_end')
        for i in range(id_user_start,id_user_end+1):
            self.get(server_url+"/movieratings?user_id="+str(i), description='Get Rating')
         
    def test_ratings_update(self):
        server_url = self.server_url
        id_user = self.conf_getInt('test_ratings_update', 'id_user')
        id_movies_rating_start = self.conf_getInt('test_ratings_update', 'id_movies_rating_start')
        id_movies_rating_stop = self.conf_getInt('test_ratings_update', 'id_movies_rating_stop')
        for i in range(id_movies_rating_start,id_movies_rating_stop+1):
            self.post(server_url+"/movieratings?user_id="+str(id_user),
             params=[
                ['score', str(randint(0,5))],
             ],
             description='Update Rating')
             
    def test_recommendations_v1(self):
        server_url = self.server_url
        id_user_start = self.conf_getInt('test_recommendations_v1', 'id_user_start')
        id_user_end = self.conf_getInt('test_recommendations_v1', 'id_user_end')
        for i in range(id_user_start,id_user_end+1):
            self.get(server_url+"/recommendations?user_id="+str(i)+"&?processing_mode=1", description='Get Recommendations v1')
            
    def test_recommendations_v2(self):
        server_url = self.server_url
        id_user_start = self.conf_getInt('test_recommendations_v2', 'id_user_start')
        id_user_end = self.conf_getInt('test_recommendations_v2', 'id_user_end')
        for i in range(id_user_start,id_user_end+1):
            self.get(server_url+"/recommendations?user_id="+str(i)+"&?processing_mode=2", description='Get Recommendations v2')
            
    def test_recommendations_v3(self):
        server_url = self.server_url
        id_user_start = self.conf_getInt('test_recommendations_v3', 'id_user_start')
        id_user_end = self.conf_getInt('test_recommendations_v3', 'id_user_end')
        for i in range(id_user_start,id_user_end+1):
            self.get(server_url+"/recommendations?user_id="+str(i)+"&?processing_mode=3", description='Get Recommendations v3')
        
        
if __name__ in ('main', '__main__'):
    unittest.main()
