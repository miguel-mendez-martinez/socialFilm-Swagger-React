import { useParams } from 'react-router-dom'
import { BadgeCheckOutline as Check, XCircleOutline as X } from '@graywolfai/react-heroicons'
import ReactPlayer from 'react-player'
import { useEffect, useState } from 'react'

import { Shell, Link, Separator } from '../../components'

import { useMovie, useComments } from '../../hooks'

import Disney from './icons/disney_plus.png'
import Play from './icons/google_play.png'
import HBO from './icons/hbo.png'
import ITunes from './icons/itunes.png'
import Netflix from './icons/netflix.png'
import Prime from './icons/prime_video.png'
import Youtube from './icons/youtube.png'

import {ListaComents } from '../../components/comment'

const backdrop = movie => {
    const backdrop = movie?.resources?.find(res => res?.type === 'BACKDROP')?.url
    const poster = movie?.resources?.find(res => res?.type === 'POSTER')?.url

    return backdrop ? backdrop : poster
}
const poster = movie => movie?.resources?.find(res => res?.type === 'POSTER')?.url

export default function EditMovie() {
    const { id } = useParams()
    const {movie, update} = useMovie(id)

    return <Shell>
        <img style = {{ height: '36rem' }}
             src = { backdrop(movie) }
             alt = { `${movie.title} backdrop` }
             className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />

        <Link variant = 'primary'
              className = 'rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
              to = {`/movies/${id}/`}>
            <Check className = 'w-8 h-8'/>
            <h1>Done</h1>
        </Link>
            
        <div className = 'mx-auto max-w-screen-2xl p-8'>
            
                <Header movie = { movie } update = {update} />
                <Info movie = { movie } update = {update} />
                <View movie = { movie } update = {update} />
                <Cast movie = { movie } />
                <Comments movie = { movie } />
        </div>
        
    </Shell>
}

function Header({ movie, update }) {
    const [title, setTitle] = useState('')

    useEffect(() => {
        setTitle(movie.title);
    }, [movie.title])

    const handleSubmit = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        let changes = [{
            op: "replace",
            path: "/title",
            value: title
        }]
        update(movie.id, changes)
    }

    return <header className = 'mt-64 relative flex items-end pb-8 mb-8'>
        <img style = {{ aspectRatio: '2/3' }}
             src = { poster(movie) }
             alt = { `${ movie.title } poster` }
             className = 'w-64 rounded-lg shadow-xl z-20' />
        <hgroup className = 'flex-1'>
            <div className='bg-black bg-opacity-50 backdrop-filter backdrop-blur flex justify-end items-center'>
                <input className = {`w-full bg-black bg-opacity-0 backdrop-filter backdrop-blur text-right text-white text-6xl font-bold p-8`}
                        value = { title }
                        onChange = {(e) => setTitle(e.target.value)}/>
                <Check className = 'w-16 h-16 text-white hover:bg-grey-700' onClick={handleSubmit} style={{cursor: 'pointer'}}/>
            </div>
            <Tagline movie = { movie } update = {update}/>
        </hgroup>
    </header>
}
function Info({ movie, update }) {
    const [argumento, setArgumento] = useState('')

    useEffect(() => {
        setArgumento(movie.overview);
    }, [movie.overview])

    const handleSubmit = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        let changes = [{
            op: "replace",
            path: "/overview",
            value: argumento
        }]
        update(movie.id, changes)
    }

    return <div className = 'grid grid-cols-5 gap-4'>
        <div className = 'col-span-4'>
            <div className='font-bold text-2xl text-white bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 shadow flex items-center'>
                <h2 className = 'p-4'>
                    Argumento
                </h2>
                <Check className = 'w-12 h-12 text-white hover:bg-grey-700'  onClick={handleSubmit} style={{cursor: 'pointer'}}/>
            </div>
            <textarea name = 'comentario' 
                        value={argumento}
                        className="w-full h-3/4 pt-8 p-4"  
                        onChange={(e) => setArgumento(e.target.value)}
                        />
        </div>
        <div className = 'text-right'>
            <dl className = 'space-y-2'>
                <CrewMember movie = { movie } job = 'Director' label = 'Dirección' update = { update }/>
                <CrewMember movie = { movie } job = 'Producer' label = 'Producción' update = { update } />
                <CrewMember movie = { movie } job = 'Screenplay' label = 'Guión' update = { update } />
                <CrewMember movie = { movie } job = 'Original Music Composer' label = 'Banda sonora' update = { update } />
            </dl>
        </div>
    </div>
}
function View({ movie, update }) {
    const trailerViejo = movie?.resources?.find(r => r.type === 'TRAILER')
    
    const [trailer, setUrlTrailer] = useState('')

    useEffect(() => {
        if(trailerViejo){
            setUrlTrailer(trailerViejo.url)
        }else{
            setUrlTrailer('')
        }
    }, [movie.overview])

    const handleSubmit = (e, movie) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        movie.resources.find(r => r.type === 'TRAILER').url = trailer
        let changes = [{
            op: "replace",
            path: "/resources",
            value: movie.resources
        }]
        //https://www.youtube.com/watch?v=6VuSUqtbXa4
        update(movie.id, changes)
    }

    return <div className = 'flex gap-4 mt-8'>
        <div className = 'w-80 z-10'>
            <Links movie = { movie } />
        </div>
        <div style = {{
                aspectRatio: '16/9'
             }}
             className = 'flex-1 ml-8 mt-8 bg-pattern-2 flex items-center justify-center z-20'>
            <input className='text-white text-xl font-semibold p-8 backdrop-filter backdrop-blur bg-red-500 bg-opacity-30 w-3/4' value={trailer} onChange={(e) => setUrlTrailer(e.target.value)}/>
            <Check className = 'w-12 h-12 text-white hover:bg-grey-700'  onClick={(e) => handleSubmit(e, movie)} style={{cursor: 'pointer'}}/>
        </div>
    </div>
}
function Cast({ movie }) {
    return <>
        <h2 className = 'mt-16 font-bold text-2xl'>Reparto principal</h2>
        <Separator />
        <ul className = 'w-full grid grid-cols-10 gap-2 overflow-hidden'>
            {
                movie?.cast?.map(person => <CastMember key = { person.name } person = { person }/>)
            }
        </ul>
    </>
}
function Comments({ movie }) {
    const { comments } = useComments({ filter: { film : movie.id } } )

    return <>
        <h2 className = 'mt-16 font-bold text-2xl'>Comentarios</h2>
        <Separator />
        <ListaComents comments = {comments} />
    </>
}
function Tagline({ movie, update }) {
    const [tagLine, setTagLine] = useState()

    useEffect(() => {
        setTagLine(movie.tagline);
    }, [movie.tagline])

    const handleSubmit = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        let changes = [{
            op: "replace",
            path: "/tagline",
            value: tagLine
        }]
        update(movie.id, changes)
    }


    if(movie.tagline) {
        return <div className='flex'>
                <input className={`block text-3xl font-semibold text-black italic w-full px-8 py-4 text-right`}
                    value={tagLine}
                    onChange = {(e) => setTagLine(e.target.value)}/>
                <Check className = 'w-16 h-16 bg-white hover:bg-grey-700' onClick={handleSubmit} style={{cursor: 'pointer'}}/>
            </div>
    } else {
        return <span className = 'block text-3xl font-semibold py-4'>&nbsp;</span>
    }
}
function CrewMember({ movie, job, label, update }) {

    const [newMember, setNewMember] = useState('')

    let oldPeople = movie?.crew?.filter(p => p.job === job)

    const handleAdd = (e) => {

        let changes = [{
            op: "add",
            path: "/crew/-",
            value: {
                id: Math.floor(1000000 + Math.random() * 9000000),
                job: job,
                name: newMember
            }
        }]
        update(movie.id, changes)

        setNewMember('')
    }

    const buscaArray = (p) => {
        let crew = movie?.crew

        let indice = crew.length - 1

        crew?.map((c, index) => {
            if(c.id == p.id && c.name == p.name){
                console.log(index)
                indice =  index
            }
        })

        return indice
    }

    const handleRemove = (e, p) => {
        //snecesitamos saber que posicion ocupa en el array de crew, para ello lo buscamos en el array
        let index = buscaArray(p)

        console.log(index)

        let changes = [{
            op: "remove",
            path: `/crew/${index}`
        }]
        update(movie.id, changes)
        setNewMember('')
    }

        return <div>
            <dt className = 'font-bold text-sm'>{ label }</dt>
                { oldPeople?.map((p, index) => <dd className = 'text-sm flex justify-end align-center p-1' key = { `${ job }/${ p.id }` }> <X className = 'w-6 h-6 mr-2' onClick={(e) => handleRemove(e, p)}  style={{cursor: 'pointer'}}/> { p.name }</dd>) }
            <div className=' mt-2 flex justify-end items-center'>
                <Check className = 'w-6 h-6 bg-white hover:bg-grey-700' onClick={handleAdd} style={{cursor: 'pointer'}}/>
                <input className='text-sm bg-black bg-opacity-20 backdrop-filter backdrop-blur text-right' value={newMember} onChange = {(e) => setNewMember(e.target.value)}/>
            </div>
        </div>
}
function Links({ movie }) {
    const resources = movie?.resources?.filter(r => !['POSTER', 'BACKDROP', 'TRAILER'].includes(r.type))
    let links

    if(resources?.length === 0) {
        links = <span className = 'block p-8 text-center bg-gray-300 font-bold'>
            No se han encontrado enlaces!
        </span>
    } else {
        links = <ul className = 'space-y-4'>
            {
                resources?.map(r => <PlatformLink key = { r.type } type = { r.type } url = { r.url } />)
            }
        </ul>
    }


    return <>
        <h2 className = 'font-bold text-2xl'>Ver ahora</h2>
        <Separator />
        { links }
    </>
}
function CastMember({ person }) {
    return <li className = 'overflow-hidden'>
        <img src = { person?.picture }
             alt = { `${person.name} profile` }
             className = 'w-full object-top object-cover rounded shadow'
             style = {{ aspectRatio: '2/3' }}/>
        <span className = 'font-bold block'> { person?.name } </span>
        <span className = 'text-sm block'> { person?.character } </span>
    </li>
}
function PlatformLink({ type = '', url = '', ...props }) {
    switch (type) {
        case 'DISNEY_PLUS':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Disney } alt = 'Disney+ logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en
                </span>
            </a>
        case 'GOOGLE_PLAY':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Play } alt = 'Google Play logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en Google Play
                </span>
            </a>
        case 'HBO':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { HBO } alt = 'HBO logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en HBO
                </span>
            </a>
        case 'ITUNES':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { ITunes } alt = 'iTunes logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en iTunes
                </span>
            </a>
        case 'NETFLIX':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Netflix } alt = 'Netflix logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en Netflix
                </span>
            </a>
        case 'PRIME_VIDEO':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Prime } alt = 'Prime Video logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en Prime Video
                </span>
            </a>
        case 'YOUTUBE':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Youtube } alt = 'YouTube logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en YouTube
                </span>
            </a>
        default: return null
    }
}
function Trailer({ movie, ...props }) {
    const trailer = movie?.resources?.find(r => r.type === 'TRAILER')

    if(trailer)
        return <ReactPlayer url = { trailer.url } { ...props } />
    else
        return <span className = 'text-white text-xl font-semibold p-8 backdrop-filter backdrop-blur bg-red-500 bg-opacity-30'>No se han encontrado trailers!</span>
}