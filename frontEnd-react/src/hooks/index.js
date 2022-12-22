import { useEffect, useState } from 'react'

import API from '../api'

export function useMovies(query = {}) {
    const [data, setData] = useState({ content: [], pagination: { hasNext: false, hasPrevious: false }})
    const queryString = JSON.stringify(query)

    useEffect(() => {
        API.instance()
            .findMovies(JSON.parse(queryString))
            .then(data => {
                setData(data)
            })
    }, [queryString])

    return data
}

export function useMovie(id = '') {
    const [data, setData] = useState({})

    useEffect(() => {
        API.instance()
            .findMovie(id)
            .then(setData)
    }, [id])

    const update = (id, changes) => API.instance()
                .updateFilm(id, changes)
                .then(movie => setData(movie))
    
    const create = (movie) => API.instance()
                .createFilm(movie)
                .then(movie => setData(movie))


    return {movie: data, update, create}
}

export function useFriendship(user=null, amigo=null){
    const [data, setData] = useState([])
    const [friendShip, setFriendShip] = useState(false)

    useEffect(() => {
        if(amigo == null){
            API.instance()
            .searchFriends(user)
            .then(data => {
                setData(data)
            })
        }else{
            API.instance()
            .areFriends(user, amigo)
            .then(data => {
                if(data == null){
                    //no son amigos
                    setFriendShip(false)
                }else{
                    //son amigos
                    setFriendShip(true)
                }
            })
        }
        
    }, [user])

    const searchFriends = user => API.instance()
            .searchFriends(user)
            .then(data => {
                setData(data)
            })

    const peticionAmistad = peticion => API.instance()
            .pedirAmistad(peticion)
            .then(data => {
                setData(data)
                setFriendShip(true)})

    const confirmarAmistad = (id, user) => API.instance()
            .confirmarAmistad(id, user)
            .then(data => setData(data))
    
    const eliminarAmistad = (id, user) => API.instance()
            .eliminarAmistad(id, user)
            .then(data => setData(data))

    const areFriends = (user, friend) => API.instance()
            .areFriends(user, friend)
            .then(data => {
                if(data == null){
                    //no son amigos
                    setFriendShip(false)
                }else{
                    //son amigos
                    setFriendShip(true)
                }
            })

    return {
        friendship: data,
        amistad: friendShip,
        searchFriends,
        eliminarAmistad,
        peticionAmistad,
        confirmarAmistad,
        areFriends
    }
}

export function useSearchUsers(id = null){
    const [data, setData] = useState([])

    useEffect(() => {
        API.instance()
            .searchUser(id)
            .then(data => {
                setData(data)
            })
    }, [id])

    const search = id => API.instance()
            .searchUser(id)
            .then(user => setData(user))

    return {
        user: data,
        search
    }
}

export function useUser(id = null) {
    const [data, setData] = useState([])
    const userId = id === null ? localStorage.getItem('user') : id

    useEffect(() => {
        API.instance()
            .findUser(userId)
            .then(data => {
                setData(data)
            })
    }, [userId])

    const create = user => API.instance()
            .createUser(user)
            .then(user => setData(user))

    const update = (id, changes) => API.instance()
            .updateUser(id, changes)
            .then(user => setData(user))

    const search = id => API.instance()
            .searchUser(id)
            .then(user => setData(user))

    return {
        user: data,
        create,
        update,
        search
    }
}

export function useComments(query = {}){
    const [data, setData] = useState({ content: [], pagination: { hasNext: false, hasPrevious: false }})
    const queryString = JSON.stringify(query)

    useEffect(() => {
        API.instance()
            .findComments(JSON.parse(queryString))
            .then(setData)
    }, [queryString])

    const create = comment => {
        console.log(comment)
        API.instance()
            .createComment(comment)
            .then( () => {
                API.instance()
                    .findComments(query)
                    .then(setData)
            })
    }

    return {
        comments: data,
        createComment: create
    }
}